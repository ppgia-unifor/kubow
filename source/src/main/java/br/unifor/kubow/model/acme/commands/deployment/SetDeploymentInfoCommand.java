package br.unifor.kubow.model.acme.commands.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.acme.element.IAcmeSystem;
import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.acmestudio.acme.model.util.core.UMSequenceValue;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

import java.io.IOException;
import java.util.*;

import static java.text.MessageFormat.format;
import static org.acmestudio.acme.PropertyHelper.toAcmeRecord;
import static org.acmestudio.acme.PropertyHelper.toAcmeVal;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class SetDeploymentInfoCommand extends AcmeModelOperation<IAcmeProperty> {

  private static final String DESIRED_REPLICAS = "desiredReplicas";
  private static final String AVAILABLE_REPLICAS = "availableReplicas";
  private static final String CURRENT_REPLICAS = "currentReplicas";
  private static final String CONTAINERS = "containers";
  private final JsonNode rawValue;
  private static ObjectReader reader = new ObjectMapper().reader();

  public SetDeploymentInfoCommand(AcmeModelInstance model, String target, String value)
      throws IOException {
    super("setDeploymentInfo", model, target, value);
    rawValue = reader.readTree(value);
  }

  @Override
  protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {
    var target = getModelContext().resolveInModel(getTarget(), IAcmeComponent.class);
    if (target == null) {
      throw new RainbowModelException(
          format("The deployment ''{0}'' could not be found in the model", getTarget()));
    }
    if (!target.declaresType("DeploymentT")) {
      throw new RainbowModelException(
          format("The deployment ''{0}'' is not of the right type.", getTarget()));
    }

    List<IAcmeCommand<?>> commands = new LinkedList<>();
    commands.add(generateCommand(target, DESIRED_REPLICAS, rawValue.get(DESIRED_REPLICAS).asInt()));
    commands.add(
        generateCommand(target, AVAILABLE_REPLICAS, rawValue.get(AVAILABLE_REPLICAS).asInt()));
    commands.add(generateCommand(target, CURRENT_REPLICAS, rawValue.get(CURRENT_REPLICAS).asInt()));
    commands.add(
        target
            .getCommandFactory()
            .propertyValueSetCommand(target.getProperty(CONTAINERS), getContainersValue(rawValue)));

    return commands;
  }

  private IAcmePropertyValue getContainersValue(JsonNode rawValue) {
    List<Map<String, String>> javaValue = getContainers(rawValue);
    List<IAcmePropertyValue> acmeValue = new ArrayList<>();

    javaValue.forEach(jv -> acmeValue.add(toAcmeRecord(jv)));
    return new UMSequenceValue(acmeValue);
  }

  private List<Map<String, String>> getContainers(JsonNode rawValue) {
    List<Map<String, String>> containers = new ArrayList<>();

    rawValue
        .get(CONTAINERS)
        .fieldNames()
        .forEachRemaining(
            c -> {
              Map<String, String> container = new HashMap<>();
              container.put("name", c);
              container.put("image", rawValue.get(CONTAINERS).get(c).asText());
              containers.add(container);
            });

    return containers;
  }

  private IAcmeCommand<?> generateCommand(IAcmeComponent target, String field, Object value) {
    var property = target.getProperty(field);
    var acmeValue = toAcmeVal(value);
    return target.getCommandFactory().propertyValueSetCommand(property, acmeValue);
  }

  @Override
  public IAcmeProperty getResult() {
    return null;
  }

  @Override
  protected boolean checkModelValidForCommand(IAcmeSystem acmeSystem) {
    return acmeSystem.declaresType("KubernetesFam");
  }
}
