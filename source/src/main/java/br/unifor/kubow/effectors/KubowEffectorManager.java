package br.unifor.kubow.effectors;

import org.acmestudio.acme.element.IAcmeElementInstance;
import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.translator.effectors.EffectorManager;

import static java.text.MessageFormat.format;
import static org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome.*;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubowEffectorManager extends EffectorManager {

  public KubowEffectorManager() {
    super("Kubow EffectorManager");
  }

  /** This method is called when an event is published by the by a publisher */
  @Override
  public OperationResult publishOperation(IRainbowOperation cmd) {
    var badResult = new OperationResult();
    badResult.result = Result.UNKNOWN;
    var actualResult = badResult;

    if (cmd.getModelReference().getModelType().equals("Acme")) {
      var ami =
          (AcmeModelInstance)
              m_modelsManagerPort.<IAcmeSystem>getModelInstance(cmd.getModelReference());
      if (ami == null) {
        String errMsg =
            format(
                "Could not find the model reference ''{0}'' for command {1}",
                cmd.getModelReference(), cmd.getName());
        m_reportingPort.error(getComponentType(), errMsg);
        badResult.reply = errMsg;
        return badResult;
      }
      try {
        Object object = ami.resolveInModel(cmd.getTarget(), Object.class);
        if (object instanceof IAcmeElementInstance) {
          actualResult = executeCommand(cmd);
        }
      } catch (Exception e) {
        badResult.reply = e.getMessage();
        actualResult = badResult;
      }
    } else {
      badResult.reply = "Currently, I only know how to effect Acme models";
    }

    return actualResult;
  }

  private OperationResult executeCommand(IRainbowOperation cmd) {
    var effector =
        m_effectors.effectors.stream()
            .filter(e -> e.getCommandPattern().getName().equals(cmd.getName()))
            .findFirst();

    var result = new OperationResult();

    if (effector.isPresent()) {
      var ea = effector.get();
      result.result = Result.SUCCESS;
      var outcome = executeEffector(ea.name, ea.getLocation(), cmd.getParameters());

      if (CONFOUNDED.equals(outcome)) {
        result.reply = "CONFOUNDED";
        result.result = Result.FAILURE;
      } else if (TIMEOUT.equals(outcome)) {
        result.reply = "TIMED OUT";
        result.result = Result.FAILURE;
      } else if (UNKNOWN.equals(outcome)) {
        result.reply = "UNKNOWN";
        result.result = Result.UNKNOWN;
      }

      if (result.result == Result.SUCCESS) {
        result.reply = cmd.toString();
      }
    } else {
      result.reply = format("No effectors understand the command {0}", cmd.getName());
      result.result = Result.FAILURE;
    }
    return result;
  }
}
