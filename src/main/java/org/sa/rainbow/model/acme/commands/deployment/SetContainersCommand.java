package org.sa.rainbow.model.acme.commands.deployment;

import com.fasterxml.jackson.databind.JsonNode;
import org.acmestudio.acme.element.property.IAcmePropertyValue;
import org.acmestudio.acme.model.util.core.UMSetValue;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.KubePropertyCommand;

import java.io.IOException;
import java.util.*;

import static org.acmestudio.acme.PropertyHelper.toAcmeRecord;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class SetContainersCommand extends KubePropertyCommand {

  public SetContainersCommand(AcmeModelInstance model, String target, String value)
      throws IOException {
    super("Deployment", "<containers>", model, target, value);
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
    Set<IAcmePropertyValue> acmeValue = new HashSet<>();

    javaValue.forEach(jv -> acmeValue.add(toAcmeRecord(jv)));
    return new UMSetValue(acmeValue);
  }
}
