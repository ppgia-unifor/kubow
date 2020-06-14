package br.unifor.kubow.adaptation;

import io.prometheus.client.Counter;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.health.IRainbowHealthProtocol;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.UtilityFunction;
import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.sa.rainbow.core.RainbowComponentT.ADAPTATION_MANAGER;

public final class UtilityBasedAdaptationManager extends AbstractRainbowRunnable
        implements IAdaptationManager<Strategy>, IRainbowModelChangeCallback {

    private static final Counter adaptationsTotal =
            Counter.build()
                    .name("rainbow_adaptations_total")
                    .help("Total of completed adaptations")
                    .labelNames("success", "strategy")
                    .register();
    private static final Counter adaptationsCycles =
            Counter.build()
                    .name("rainbow_adaptations_cycles")
                    .help("Total of adaptation cycles.")
                    .labelNames("available_strategies", "applicable_strategies", "selected_strategy")
                    .register();

    private static final String NAME = "Kube Rainbow Adaptation Manager";
    private static final double FAILURE_RATE_THRESHOLD = 0.95;
    private static final double MIN_UTILITY_THRESHOLD = 0.40;
    private static final long FAILURE_EFFECTIVE_WINDOW = 2000 /* ms */;
    private static final long FAILURE_WINDOW_CHUNK = 1000 /* ms */;
    /**
     * The prefix to be used by the strategy which takes a "leap" by achieving a similar adaptation
     * that would have taken multiple increments of the non-leap version, but at a potential "cost" in
     * non-dire scenarios.
     */
    private static final String LEAP_STRATEGY_PREFIX = "Leap-";
    /** The prefix to represent the corresponding multi-step strategy of the leap-version strategy. */
    private static final String MULTI_STRATEGY_PREFIX = "Multi-";

    private static final int SLEEP_TIME = Rainbow.instance().getProperty("customize.adaptation.cycletime", 10000);
    private static final int I_RUN = 0;
    private static final int I_SUCCESS = 1;
    private static final int I_FAIL = 2;
    private static final int I_OTHER = 3;
    private static final int CNT_I = 4;
    private static double m_minUtilityThreshold = 0.0;
    private final List<Strategy> availableStrategies;
    private AcmeModelInstance m_model;
    private boolean m_adaptNeeded;
    // constraint being violated
    private boolean m_adaptEnabled;
    private List<Stitch> m_repertoire;
    private List<AdaptationTree<Strategy>> m_pendingStrategies;
    private String m_historyTrackUtilName;
    private Map<String, int[]> m_historyCnt;
    private Map<String, Beacon> m_failTimer;
    private IRainbowAdaptationEnqueuePort<Strategy> m_enqueuePort;
    private IModelChangeBusSubscriberPort m_modelChangePort;
    private IModelsManagerPort m_modelsManagerPort;
    private String m_modelRef;
    private IRainbowChangeBusSubscription m_modelTypecheckingChanged =
            new IRainbowChangeBusSubscription() {

                @Override
                public boolean matches(IRainbowMessage message) {
                    String type = (String) message.getProperty(IModelChangeBusPort.EVENT_TYPE_PROP);
                    String modelName = (String) message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP);
                    String modelType = (String) message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP);
                    try {
                        CommandEventT ct = CommandEventT.valueOf(type);
                        return (ct.isEnd()
                                && "setTypecheckResult"
                                .equals(message.getProperty(IModelChangeBusPort.COMMAND_PROP))
                                && m_modelRef.equals(Util.genModelRef(modelName, modelType)));
                    } catch (Exception e) {
                        return false;
                    }
                }
            };
    private UtilityPreferenceDescription m_utilityModel;

    /** Default constructor. */
    public UtilityBasedAdaptationManager() {
        super(NAME);
        availableStrategies = new ArrayList<>();
        m_adaptEnabled = true;
        m_repertoire = new ArrayList<>();
        m_pendingStrategies = new ArrayList<>();
        m_historyTrackUtilName =
                Rainbow.instance().getProperty(RainbowConstants.PROPKEY_TRACK_STRATEGY);
        if (m_historyTrackUtilName != null) {
            m_historyCnt = new HashMap<>();
            m_failTimer = new HashMap<>();
        }
        String thresholdStr =
                Rainbow.instance().getProperty(RainbowConstants.PROPKEY_UTILITY_MINSCORE_THRESHOLD);
        if (thresholdStr == null) {
            m_minUtilityThreshold = MIN_UTILITY_THRESHOLD;
        } else {
            m_minUtilityThreshold = Double.valueOf(thresholdStr);
        }
        setSleepTime(SLEEP_TIME);
    }

    @Override
    public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize(port);
        initConnectors();
    }

    private void initConnectors() throws RainbowConnectionException {
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
        m_modelChangePort.subscribe(m_modelTypecheckingChanged, this);
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
    }

    @Override
    public void setModelToManage(ModelReference model) {
        m_modelRef = model.getModelName() + ":" + model.getModelType();
        m_model = (AcmeModelInstance) m_modelsManagerPort.<IAcmeSystem>getModelInstance(model);
        if (m_model == null) {
            m_reportingPort.error(
                    ADAPTATION_MANAGER,
                    MessageFormat.format("Could not find reference to {0}", model.toString()));
        }
        m_enqueuePort = RainbowPortFactory.createAdaptationEnqueuePort(model);
        ModelReference utilityModelRef = new ModelReference(model.getModelName(), "UtilityModel");
        IModelInstance<UtilityPreferenceDescription> modelInstance =
                m_modelsManagerPort.getModelInstance(utilityModelRef);
        if (modelInstance == null) {
            m_reportingPort.error(
                    ADAPTATION_MANAGER,
                    MessageFormat.format(
                            "There is no utility model associated with this model. Expecting to find "
                                    + "''{0}''. Perhaps it is not specified in the rainbow.properties "
                                    + "file?",
                            utilityModelRef.toString()));

        } else {
            m_utilityModel = modelInstance.getModelInstance();
        }
        initAdaptationRepertoire();
    }

    @Override
    public ModelReference getManagedModel() {
        return ModelReference.fromString(m_modelRef);
    }

    @Override
    public boolean isEnabled() {
        return m_adaptEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        m_reportingPort.info(
                getComponentType(),
                MessageFormat.format("Turning adaptation {0}.", (enabled ? "on" : "off")));
        if (!enabled && !m_pendingStrategies.isEmpty()) {
            m_reportingPort.info(
                    getComponentType(), "There is an adaptation in progress. This will finish.");
        }
        m_adaptEnabled = enabled;
    }

    public void setAdaptationEnabled(boolean b) {
        m_adaptEnabled = b;
    }

    public boolean adaptationInProgress() {
        return m_adaptNeeded;
    }

    /**
     * Removes a Strategy from the list of pending strategies, marking it as being completed (doesn't
     * incorporate outcome).
     *
     * @param strategy the strategy to mark as being executed.
     */
    @Override
    public void markStrategyExecuted(AdaptationTree<Strategy> strategy) {
        if (m_pendingStrategies.contains(strategy)) {
            m_pendingStrategies.remove(strategy);

            adaptationsTotal.labels(Boolean.TRUE.toString(), strategy.getHead().getName()).inc();
            final List<Strategy> strategiesExecuted = new LinkedList<>();
            final CountDownLatch countdownLatch = new CountDownLatch(1);
            DefaultAdaptationExecutorVisitor<Strategy> resultCollector =
                    new UtilityBasedAdaptationManager.StrategyAdaptationResultsVisitor(
                            strategy, countdownLatch, strategiesExecuted);
            resultCollector.start();
            try {
                countdownLatch.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }

            for (Strategy str : strategiesExecuted) {
                String s = str.getName() + ";" + str.outcome();
                log("*S* outcome: " + s);
                Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY + s);
                tallyStrategyOutcome(str);
            }
        }
        if (m_pendingStrategies.isEmpty()) {
            Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_END);
            m_adaptNeeded = false;
        }
    }

    private boolean canAdapt() {
        return m_adaptEnabled && m_pendingStrategies.isEmpty();
    }

    @Override
    protected void runAction() {
        if (!canAdapt()) {
            return;
        }
        Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_BEGIN);
        var selectedStrategy = selectStrategy();
        Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SELECTION_END);
        if (selectedStrategy != null) {
            log(">> do strategy: " + selectedStrategy.getName());

            var adaptation = new AdaptationTree<>(selectedStrategy);
            m_pendingStrategies.add(adaptation);
            m_enqueuePort.offerAdaptation(adaptation, null);
            String logMessage = selectedStrategy.getName();
            log(logMessage);
        }
    }

    /*
     * Algorithm: - Iterate through repertoire searching for enabled strategies,
     * where "enabled" means applicable to current system condition NOTE: A
     * Strategy is "applicable" iff the conditions of applicability of the root
     * tactic is true TODO: Need to check if above is good assumption -
     * Calculate scores of the enabled strategies = this involves evaluating the
     * meta-information of the tactics in each strategy - Select and execute the
     * highest scoring strategy
     */
    private Strategy selectStrategy() {
        log("Checking if adaptation is required");

        Map<String, Strategy> appSubsetByName = new HashMap<>();
        final Map<String, Object> params = Map.of();
        for (Strategy strategy : availableStrategies) {
            if (getFailureRate(strategy) > FAILURE_RATE_THRESHOLD) {
                continue;
            }
            if (strategy.isApplicable(params)) {
                appSubsetByName.put(strategy.getName(), strategy);
            }
        }

        if (appSubsetByName.size() == 0) {
            log("No applicable Strategies to do an adaptation");
            adaptationsCycles.labels(valueOf(availableStrategies.size()), "0", "").inc();
            return null;
        }

        // check for leap-version strategy to see whether to "chain" util
        // computation
        Set<String> applicableSubsetNames = appSubsetByName.keySet();
        for (String name : applicableSubsetNames.toArray(new String[applicableSubsetNames.size()])) {
            Strategy strategy = appSubsetByName.get(name);
            Strategy leap = appSubsetByName.get(LEAP_STRATEGY_PREFIX + name);
            if (leap != null) { // Leap-version exists
                /*
                 * To chain: Determine the integer multiple N of Leap over this,
                 * then compute aggregate attributes using previous attributes
                 * as the starting point, repeating N-1 times.
                 */
                // HACK: use the first argument of the tactic closest to root
                int factor = 1;
                double stratArgVal = strategy.getFirstTacticArgumentValue();
                double leapArgVal = leap.getFirstTacticArgumentValue();
                if (stratArgVal != Double.NaN) {
                    // compute multiple now
                    factor = (int) (leapArgVal / stratArgVal);
                }
                Strategy multi = strategy.clone(strategy.parent());
                multi.setName(MULTI_STRATEGY_PREFIX + strategy.getName());
                multi.multiples = factor;
                appSubsetByName.put(multi.getName(), multi);
            }
        }

        Optional<Strategy> selected;
        if (Rainbow.instance().getProperty("customize.utility.enabled", false)) {
            selected = ofNullable(selectByUtilityFunction(appSubsetByName));
        } else {
            selected = ofNullable(appSubsetByName.get(appSubsetByName.keySet().iterator().next()));
        }
        if (selected.isPresent()) {
            adaptationsCycles
                    .labels(
                            valueOf(availableStrategies.size()),
                            valueOf(appSubsetByName.size()),
                            selected.get().getName())
                    .inc();
            return selected.get();
        }
        adaptationsCycles.labels(valueOf(availableStrategies.size()), valueOf(appSubsetByName.size()), "").inc();
        return null;
    }

    private Strategy selectByUtilityFunction(Map<String, Strategy> appSubsetByName) {
        SortedMap<Double, Strategy> scoredStrategies = scoreStrategies(appSubsetByName);
        if (Util.dataLogger().isInfoEnabled()) {
            StringBuilder buf = new StringBuilder();
            buf.append("  [\n");
            for (Map.Entry<Double, Strategy> entry : scoredStrategies.entrySet()) {
                buf.append("   ").append(entry.getValue().getName()).append(":");
                buf.append(entry.getKey()).append("\n");
            }
            buf.append("  ]\n");
            log(buf.toString());
            Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_SCORE + buf.toString());
        }

        if (scoredStrategies.size() > 0) {
            return scoredStrategies.get(scoredStrategies.lastKey());
        } else {
            Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_END);
            log("<< NO applicable strategy, adaptation cycle ended.");
            return null;
        }
    }

    /**
     * Iterate through the supplied set of strategies, compute aggregate attributes, and use the
     * aggregate values plus stakeholder utility preferences to compute an integer score for each
     * Strategy, between 0 and 100.
     *
     * @param subset the subset of condition-applicable Strategies to score, in the form of a
     *     name-strategy map
     * @return a map of score-strategy pairs, sorted in increasing order by score.
     */
    private SortedMap<Double, Strategy> scoreStrategies(Map<String, Strategy> subset) {
        String scenario = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCENARIO);
        return scoreForScenario(scenario, subset);
    }

    private SortedMap<Double, Strategy> scoreForScenario(
            String scenario, Map<String, Strategy> subset) {
        Map<String, Double> weights = m_utilityModel.weights.get(scenario);
        SortedMap<Double, Strategy> scored = new TreeMap<>();
        double[] conds = null; // store the conditions to output for diagnosis

        // find the weights of the applicable scenario
        log("Scoring for " + scenario);
        for (Strategy strategy : subset.values()) {
            SortedMap<String, Double> aggAtt = strategy.computeAggregateAttributes();
            // add the strategy failure history as another attribute
            accountForStrategyHistory(aggAtt, strategy);
            String s = strategy.getName() + aggAtt;
            Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY_ATTR + s);
            log("aggAttr: " + s);
            /*
             * compute utility values from attributes that combines values
             * representing current condition, then accumulate the weighted
             * utility sum
             */
            double[] items = new double[aggAtt.size()];
            double[] utilityOfItem = new double[aggAtt.size()];
            double[] currentUtility = new double[aggAtt.size()];
            if (conds == null) {
                conds = new double[aggAtt.size()];
            }
            int i = 0;
            double score = 0.0;
            for (String k : aggAtt.keySet()) {
                double v = aggAtt.get(k);
                // find the applicable utility function
                UtilityFunction u = m_utilityModel.getUtilityFunctions().get(k);
                if (u == null) {
                    log("Error: attempting to calculate for not existent function: " + k);
                    continue;
                }
                Object condVal;
                // add attribute value from CURRENT condition to accumulated agg
                // value
                condVal = m_model.getProperty(u.mapping());
                items[i] = v;
                if (condVal != null) {
                    double val = 0.0;
                    if (condVal instanceof Double) {
                        val = (Double) condVal;
                    } else if (condVal instanceof Float) {
                        val = ((Float) condVal).doubleValue();
                    } else if (condVal instanceof Integer) {
                        val = ((Integer) condVal).doubleValue();
                    }

                    m_reportingPort.trace(
                            getComponentType(), "Avg value of prop: " + u.mapping() + " == " + condVal);
                    conds[i] = val;
                    items[i] += conds[i];
                }
                // now compute the utility, apply weight, and accumulate to sum
                utilityOfItem[i] = u.f(items[i]);
                currentUtility[i] = u.f(conds[i]);
                score += weights.get(k) * utilityOfItem[i];
                ++i;
            }

            // log this
            s = Arrays.toString(items);
            if (score < m_minUtilityThreshold) {
                // utility score too low, don't consider for adaptation
                log("score " + score + " below threshold, discarding: " + s);
            } else {
                scored.put(score, strategy);
            }
            log("current model properties: " + Arrays.toString(conds));
            log("current model utilities: " + Arrays.toString(currentUtility));
            log(strategy.getName() + ": predicted utilities: " + Arrays.toString(utilityOfItem));
            log(strategy.getName() + ": score = " + score);
            Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STRATEGY_ATTR2 + s);
            log("aggAtt': " + s);
        }
        log("cond   : " + Arrays.toString(conds));

        return scored;
    }

    /**
     * Retrieves the adaptation repertoire; for each tactic, store the respective tactic attribute
     * vectors.
     */
    private void initAdaptationRepertoire() {
        File stitchPath =
                Util.getRelativeToPath(
                        Rainbow.instance().getTargetPath(),
                        Rainbow.instance().getProperty(RainbowConstants.PROPKEY_SCRIPT_PATH));
        if (stitchPath == null) {
            m_reportingPort.error(ADAPTATION_MANAGER, "The stitchState path is not set!");
        } else if (stitchPath.exists() && stitchPath.isDirectory()) {
            // find only ".s" files
            FilenameFilter ff = (dir, name) -> name.endsWith(".s");
            for (File f : stitchPath.listFiles(ff)) {
                try {
                    // don't duplicate loading of script files
                    Stitch stitch = Ohana.instance().findStitch(f.getCanonicalPath());
                    if (stitch == null) {
                        DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler();
                        stitch = Stitch.newInstance(f.getCanonicalPath(), stitchProblemHandler);
                        Ohana.instance().parseFile(stitch);
                        reportProblems(f, stitchProblemHandler);

                        // apply attribute vectors to tactics, if available
                        defineAttributes(stitch, m_utilityModel.attributeVectors);
                        if (stitch.script.isApplicableForSystem(m_model)) {
                            m_repertoire.add(stitch);
                            availableStrategies.addAll(stitch.script.strategies);
                            log("Parsed script " + stitch.path);
                        }
                    } else {
                        log("Previously known script " + stitch.path);
                    }
                } catch (IOException e) {
                    m_reportingPort.error(
                            ADAPTATION_MANAGER, "Obtaining file canonical path failed! " + f.getName(), e);
                }
            }
        }
    }

    private void reportProblems(File f, DummyStitchProblemHandler sph) {

        Collection<IStitchProblem> problem = sph.getProblems();
        if (!problem.isEmpty()) {
            log("Errors exist in strategy: " + f.getName() + ", or one of its included files");
        }
        for (IStitchProblem p : problem) {
            StringBuilder out = new StringBuilder();
            switch (p.getSeverity()) {
                case IStitchProblem.ERROR:
                    out.append("ERROR: ");
                    break;
                case IStitchProblem.WARNING:
                    out.append("WARNING: ");
                    break;
                case IStitchProblem.FATAL:
                    out.append("FATAL ERROR: ");
                    break;
                case IStitchProblem.UNKNOWN:
                    out.append("UNKNOWN PROBLEM: ");
                    break;
            }
            out.append("Line: ").append(p.getLine());
            out.append(", ");
            out.append(" Column: ").append(p.getColumn());
            out.append(": ").append(p.getMessage());
            log(out.toString());
        }
        sph.clearProblems();
    }

    private void defineAttributes(Stitch stitch, Map<String, Map<String, Object>> attrVectorMap) {
        for (Tactic t : stitch.script.tactics) {
            Map<String, Object> attributes = attrVectorMap.get(t.getName());
            if (attributes != null) {
                // found attribute def for tactic, save all key-value pairs
                m_reportingPort.trace(
                        getComponentType(), "Found attributes for tactic " + t.getName() + ", saving pairs...");
                for (Map.Entry<String, Object> e : attributes.entrySet()) {
                    t.putAttribute(e.getKey(), e.getValue());
                    m_reportingPort.trace(
                            getComponentType(), " - (" + e.getKey() + ", " + e.getValue() + ")");
                }
            }
        }
    }

    private void tallyStrategyOutcome(Strategy s) {
        if (m_historyTrackUtilName == null) return;

        String name = s.getName();
        // mark timer of failure, if applicable
        Beacon timer = m_failTimer.get(name);
        if (timer == null) {
            timer = new Beacon();
            m_failTimer.put(name, timer);
        }
        // get the stats array for this strategy
        int[] stat = m_historyCnt.get(name);
        if (stat == null) {
            stat = new int[CNT_I];
            stat[I_RUN] = 0;
            stat[I_SUCCESS] = 0;
            stat[I_FAIL] = 0;
            stat[I_OTHER] = 0;
            m_historyCnt.put(name, stat);
        }
        // tally outcome counts
        ++stat[I_RUN];
        switch (s.outcome()) {
            case SUCCESS:
                ++stat[I_SUCCESS];
                break;
            case FAILURE:
                ++stat[I_FAIL];
                timer.mark();
                break;
            default:
                ++stat[I_OTHER];
                break;
        }
        String str = name + Arrays.toString(stat);
        log("History: " + str);
        Util.dataLogger().info(IRainbowHealthProtocol.DATA_ADAPTATION_STAT + str);
    }

    private void accountForStrategyHistory(Map<String, Double> aggAtt, Strategy s) {
        if (m_historyTrackUtilName == null) return;

        if (m_historyCnt.containsKey(s.getName())) {
            // consider failure only
            aggAtt.put(m_historyTrackUtilName, getFailureRate(s));
        } else {
            // consider no failure
            aggAtt.put(m_historyTrackUtilName, 0.0);
        }
    }

    private double getFailureRate(Strategy s) {
        double rv = 0.0;
        if (m_historyTrackUtilName == null) return rv;

        int[] stat = m_historyCnt.get(s.getName());
        if (stat != null) {
            Beacon timer = m_failTimer.get(s.getName());
            double factor = 1.0;
            long failTimeDelta = timer.elapsedTime() - FAILURE_EFFECTIVE_WINDOW;
            if (failTimeDelta > 0) {
                factor = FAILURE_WINDOW_CHUNK * 1.0 / failTimeDelta;
            }
            rv = factor * stat[I_FAIL] / stat[I_RUN];
        }
        return rv;
    }

    @Override
    public void onEvent(ModelReference mr, IRainbowMessage message) {
        // Because of the subscription, the model should be the model ref so no need to check
        String typecheckSt = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + "0");
        Boolean typechecks = Boolean.valueOf(typecheckSt);
        // Cause the thread to wake up if it is sleeping
        if (!typechecks) {
            activeThread().interrupt();
        }
    }

    @Override
    public RainbowComponentT getComponentType() {
        return ADAPTATION_MANAGER;
    }

    @Override
    protected void log(String txt) {
        m_reportingPort.info(ADAPTATION_MANAGER, txt);
    }

    @Override
    public void dispose() {
        for (Stitch stitch : m_repertoire) {
            stitch.dispose();
        }
        Ohana.instance().dispose();
        m_repertoire.clear();
        m_pendingStrategies.clear();
        if (m_historyTrackUtilName != null) {
            m_historyCnt.clear();
            m_failTimer.clear();
            m_historyCnt = null;
            m_failTimer = null;
        }

        if (m_enqueuePort != null) {
            m_enqueuePort.dispose();
        }
        m_modelChangePort.dispose();

        // null-out data members
        m_repertoire = null;
        m_pendingStrategies = null;
        m_historyTrackUtilName = null;
        m_model = null;
    }

    private class StrategyAdaptationResultsVisitor
            extends DefaultAdaptationExecutorVisitor<Strategy> {
        private final List<Strategy> m_strategiesExecuted;

        StrategyAdaptationResultsVisitor(
                AdaptationTree<Strategy> strategy,
                CountDownLatch countdownLatch,
                List<Strategy> strategiesExecuted) {
            super(
                    strategy,
                    UtilityBasedAdaptationManager.this.activeThread().getThreadGroup(),
                    "",
                    countdownLatch,
                    UtilityBasedAdaptationManager.this.m_reportingPort);
            m_strategiesExecuted = strategiesExecuted;
        }

        @Override
        protected boolean evaluate(Strategy adaptation) {
            if (adaptation.outcome() != Strategy.Outcome.UNKNOWN) {
                synchronized (m_strategiesExecuted) {
                    m_strategiesExecuted.add(adaptation);
                }
            }
            return true;
        }

        @Override
        protected DefaultAdaptationExecutorVisitor<Strategy> spawnNewExecutorForTree(
                AdaptationTree<Strategy> adt, ThreadGroup g, CountDownLatch doneSignal) {
            return new UtilityBasedAdaptationManager.StrategyAdaptationResultsVisitor(
                    adt, doneSignal, m_strategiesExecuted);
        }
    }
}
