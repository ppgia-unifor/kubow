package org.sa.rainbow.translator;

import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.EffectorDescription;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.translator.effectors.EffectorManager;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.util.Util;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class AcmeEffectorManager extends EffectorManager {

    public AcmeEffectorManager() {
        super("Acme Global Effector Manager");
    }

    /**
     * This method is called when an event is published by the by a publisher
     */
    @Override
    public OperationResult publishOperation(IRainbowOperation cmd) {
        OperationResult badResult = new OperationResult();
        badResult.result = Result.UNKNOWN;
        OperationResult actualResult = badResult;

        if (cmd.getModelReference().getModelType().equals("Acme")) {
            AcmeModelInstance ami =
                    (AcmeModelInstance)
                            m_modelsManagerPort.<IAcmeSystem>getModelInstance(cmd.getModelReference());
            if (ami == null) {
                String errMsg =
                        MessageFormat.format(
                                "Could not find the model reference ''{0}'' for command {1}",
                                Util.genModelRef(
                                        cmd.getModelReference().getModelName(), cmd.getModelReference().getModelType()),
                                cmd.getName());
                m_reportingPort.error(getComponentType(), errMsg);
                badResult.reply = errMsg;
                return badResult;
            }
            try {
                Object object = ami.resolveInModel(cmd.getTarget(), Object.class);
                if (object instanceof IAcmeElementInstance) {
                    String location = "127.0.0.1";

                    // look for an effector registered at this location that understands the command
                    cmd = resolveElementReferences(cmd, ami);
                    var effectors = getEffectorsAtLocation(location);

                    effectors.addAll(getEffectorsInterestedInLocation(location, m_effectors.effectors));

                    filterEffectorsBasedOnCommandName(cmd, effectors);
                    filterEffectorsBasedOnCommandParameters(cmd, effectors, ami);

                    if (!effectors.isEmpty()) {
                        var result = new OperationResult();
                        result.result = Result.SUCCESS;
                        var errMsg = new StringBuilder();
                        for (EffectorDescription.EffectorAttributes ea : effectors) {
                            IEffectorExecutionPort.Outcome outcome =
                                    executeEffector(ea.name, ea.getLocation(), cmd.getParameters());
                            if (outcome != IEffectorExecutionPort.Outcome.SUCCESS) {
                                errMsg.append(
                                        MessageFormat.format(
                                                "E[{0}@{1}]: Failed to execute command {2} - ",
                                                ea.name, location, Arrays.toString(cmd.getParameters())));

                                switch (outcome) {
                                    case CONFOUNDED:
                                        errMsg.append("CONFOUNDED");
                                        result.result = Result.FAILURE;
                                        break;
                                    case TIMEOUT:
                                        errMsg.append("TIMED OUT");
                                        result.result = Result.FAILURE;
                                        break;
                                    case UNKNOWN:
                                        errMsg.append("UNKNOWN");
                                        if (result.result != Result.FAILURE) {
                                            result.result = Result.UNKNOWN;
                                        }
                                        break;
                                }
                                errMsg.append("\n");
                            }
                        }
                        if (result.result == Result.FAILURE) {
                            result.reply = errMsg.toString();
                        } else result.reply = cmd.toString();
                        actualResult = result;
                    } else {
                        badResult.reply =
                                MessageFormat.format(
                                        "No effectors at {0} understand the command {1}", location, cmd.getName());
                        actualResult = badResult;
                    }
                }
            } catch (Exception e) {
                badResult.reply = e.getMessage();
                actualResult = badResult;
            }

        } else {
            badResult.reply = "Currently, I only know how to effect Acme models";
            actualResult = badResult;
        }

        return actualResult;
    }

    private IRainbowOperation resolveElementReferences(IRainbowOperation cmd, AcmeModelInstance ami) {
        String target = cmd.getTarget();
        target = resolveAcmeReference(target, ami);
        String[] args = cmd.getParameters();
        for (int i = 0; i < args.length; i++) {
            if (!"".equals(args[i])) {
                try {
                    args[i] = resolveAcmeReference(args[i], ami);
                } catch (Throwable e) {
                    // Some arguments aren't models
                }
            }
        }
        return new OperationRepresentation(cmd.getName(), cmd.getModelReference(), target, args);
    }

    private String resolveAcmeReference(String target, AcmeModelInstance ami) {
        try {
            Object modelObject = ami.resolveInModel(target, Object.class);
            if (modelObject instanceof IAcmeElementInstance) {
                String location = ami.getElementLocation((IAcmeElementInstance<?, ?>) modelObject);
                if (location != null) {
                    target = location;
                }
            } else if (modelObject instanceof IAcmeProperty) {
                IAcmeProperty prop = (IAcmeProperty) modelObject;
                if (prop.getValue() != null) {
                    target = ModelHelper.propertyValueAsString(prop.getValue());
                }
            }
        } catch (RainbowModelException e) {
            log("Tried to resolve " + target + " as an Acme reference, but it isn't one. Leaving alone.");
        }
        return target;
    }

    /**
     * Returns all the effectors that are registered at the location
     *
     * @param location
     * @param effectors
     * @return
     */
    private Collection<? extends EffectorDescription.EffectorAttributes>
    getEffectorsInterestedInLocation(
            String location, Set<EffectorDescription.EffectorAttributes> effectors) {
        Set<EffectorDescription.EffectorAttributes> interestedIn = new HashSet<>();
        for (EffectorDescription.EffectorAttributes ea : effectors) {
            if (ea.getCommandPattern() != null && location.equals(ea.getCommandPattern().getTarget())) {
                interestedIn.add(ea);
            }
        }
        return interestedIn;
    }

    private void filterEffectorsBasedOnCommandName(
            IRainbowOperation cmd, Set<EffectorDescription.EffectorAttributes> effectors) {
        for (Iterator iterator = effectors.iterator(); iterator.hasNext(); ) {
            EffectorDescription.EffectorAttributes ea =
                    (EffectorDescription.EffectorAttributes) iterator.next();
            if (ea.getCommandPattern() == null
                    || !ea.getCommandPattern().getName().equals(cmd.getName())) {
                iterator.remove();
            }
        }
    }

    /**
     * Filter so that all the effectors that match the command targets and parameters remain in the
     * set
     *
     * @param cmd       The cmd to match
     * @param effectors The set of effectors, which will be mutated by this method
     * @param ami       The model used to resolve any names
     */
    private void filterEffectorsBasedOnCommandParameters(
            IRainbowOperation cmd,
            Set<EffectorDescription.EffectorAttributes> effectors,
            AcmeModelInstance ami) {
        for (Iterator iterator = effectors.iterator(); iterator.hasNext(); ) {
            EffectorDescription.EffectorAttributes ea =
                    (EffectorDescription.EffectorAttributes) iterator.next();
            OperationRepresentation commandPattern = ea.getCommandPattern();
            if (commandPattern != null) {
                String target = commandPattern.getTarget();
                int idxStart = 0;
                if (target == null /*|| !target.equals (ea.location)*/) {
                    target = commandPattern.getParameters()[0];
                    idxStart = 1;
                }
                if (target != null && !target.contains("$<")) {
                    if (!target.equals(cmd.getTarget())) {
                        if (cmd.getTarget() != null) {
                            String resolvedTarget = resolveAcmeReference(target, ami);
                            if (!resolvedTarget.equals(cmd.getTarget())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
                String[] parameters = commandPattern.getParameters();
                if (parameters != null) {
                    boolean removed = false;
                    for (int i = idxStart; i < parameters.length && !removed; i++) {
                        if (!parameters[i].contains("$<"))
                            if (i - idxStart < cmd.getParameters().length
                                    && !parameters[i].equals(cmd.getParameters()[i - idxStart])) {
                                iterator.remove();
                                break;
                            }
                    }
                }
            }
        }
    }

    //    @Override
    //    public void publishMessage (IRainbowMessage msg) {
    //
    //    }
}
