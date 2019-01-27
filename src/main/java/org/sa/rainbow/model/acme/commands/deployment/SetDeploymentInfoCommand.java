package org.sa.rainbow.model.acme.commands.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.KubeAcmeModelCommand;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static java.text.MessageFormat.format;
import static org.acmestudio.acme.PropertyHelper.toAcmeVal;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class SetDeploymentInfoCommand extends KubeAcmeModelCommand<IAcmeProperty> {

  private final JsonNode rawValue;

  public SetDeploymentInfoCommand(AcmeModelInstance model, String target, String value)
      throws IOException {
    super("setDeploymentInfo", model, target, value);
    rawValue = new ObjectMapper().readTree(value);
  }

  @Override
  protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {
    var target = getModelContext().resolveInModel(getTarget(), IAcmeComponent.class);
    if (target == null) {
      throw new RainbowModelException(
          format("The deployment ''{0}'' could not be found in the model", getTarget()));
    }
    if (!target.declaresType("Deployment")) {
      throw new RainbowModelException(
          format("The deployment ''{0}'' is not of the right type.", getTarget()));
    }

    List<IAcmeCommand<?>> commands = new LinkedList<>();
    commands.add(
        generateCommand(target, "desiredReplicas", rawValue.get("desiredReplicas").asInt()));
    commands.add(
        generateCommand(target, "availableReplicas", rawValue.get("availableReplicas").asInt()));
    commands.add(
        generateCommand(target, "currentReplicas", rawValue.get("currentReplicas").asInt()));
    return commands;
  }

  private IAcmeCommand<?> generateCommand(IAcmeComponent target, String field, Object value) {
    var property = target.getProperty(field);
    var acmeValue = toAcmeVal(value);
    return target.getCommandFactory().propertyValueSetCommand(property, acmeValue);
  }

  @Override
  public IAcmeProperty getResult() throws IllegalStateException {
    return null;
  }
}
