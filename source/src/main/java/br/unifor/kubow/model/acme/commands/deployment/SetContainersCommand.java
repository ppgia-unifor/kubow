package br.unifor.kubow.model.acme.commands.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.util.core.UMSequenceValue;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.KubowPropertyCommand;

import java.io.IOException;
import java.util.*;

import static org.acmestudio.acme.PropertyHelper.toAcmeRecord;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class SetContainersCommand extends KubowPropertyCommand {

  public SetContainersCommand(AcmeModelInstance model, String target, String value)
      throws IOException {
    super("DeploymentT", "<containers>", model, target, value);
  }

  @Override
  protected Object extractValue(JsonNode rawValue) {
    List<Map<String, String>> containers = new ArrayList<>();

    rawValue
        .get("containers")
        .fieldNames()
        .forEachRemaining(
            c -> {
              Map<String, String> container = new HashMap<>();
              container.put("name", c);
              container.put("image", rawValue.get("containers").get(c).asText());
              containers.add(container);
            });

    return containers;
  }

  @Override
  protected IAcmePropertyValue getAcmeValue(Object value) {
    List<Map<String, String>> javaValue = (List<Map<String, String>>) value;
    List<IAcmePropertyValue> acmeValue = new ArrayList<>();

    javaValue.forEach(jv -> acmeValue.add(toAcmeRecord(jv)));
    return new UMSequenceValue(acmeValue);
  }
}
