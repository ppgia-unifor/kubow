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
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeRainbowOperationEvent.CommandEventT;
import org.sa.rainbow.stitch.Ohana;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.error.DummyStitchProblemHandler;
import org.sa.rainbow.stitch.error.IStitchProblem;
import org.sa.rainbow.stitch.visitor.Stitch;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static org.sa.rainbow.core.RainbowComponentT.ADAPTATION_MANAGER;

public final class AdaptationManager extends AbstractRainbowRunnable
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

  private static final double FAILURE_RATE_THRESHOLD = 0.95;
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

  private final List<Strategy> availableStrategies;
  private AcmeModelInstance m_model;
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

  public AdaptationManager() {
    super("Kubow Adaptation Manager");
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
          new AdaptationManager.StrategyAdaptationResultsVisitor(
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

    Optional<Strategy> selected = ofNullable(appSubsetByName.get(appSubsetByName.keySet().iterator().next()));

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

      for (File f : stitchPath.listFiles((dir, name) -> name.endsWith(".s"))) {
        try {
          // don't duplicate loading of script files
          Stitch stitch = Ohana.instance().findStitch(f.getCanonicalPath());
          if (stitch == null) {
            DummyStitchProblemHandler stitchProblemHandler = new DummyStitchProblemHandler();
            stitch = Stitch.newInstance(f.getCanonicalPath(), stitchProblemHandler);
            Ohana.instance().parseFile(stitch);
            reportProblems(f, stitchProblemHandler);

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
          AdaptationManager.this.activeThread().getThreadGroup(),
          "",
          countdownLatch,
          AdaptationManager.this.m_reportingPort);
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
      return new AdaptationManager.StrategyAdaptationResultsVisitor(
          adt, doneSignal, m_strategiesExecuted);
    }
  }
}
