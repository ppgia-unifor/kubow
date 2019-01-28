package org.sa.rainbow.translator.gauges;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubeGauge extends AbstractJsonGaugeWithProbes {

  private static final String NAME = "G - Kubernetes Gauge";
  private final Set<String> commands;
  private String propertyPath = "";
  private String propertyName = "";

  public KubeGauge(
      String id,
      long beaconPeriod,
      TypedAttribute gaugeDesc,
      TypedAttribute modelDesc,
      List<TypedAttributeWithValue> setupParams,
      Map<String, IRainbowOperation> mappings)
      throws RainbowException {
    super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    commands = mappings.keySet();
    setupParams.stream()
        .filter(p -> p.getName().equals("propertyPath"))
        .findFirst()
        .ifPresent(v -> propertyPath = v.getValue().toString());
    setupParams.stream()
            .filter(p -> p.getName().equals("propertyName"))
            .findFirst()
            .ifPresent(v -> propertyName = v.getValue().toString());
  }

  @Override
  protected void runAction() {
    int maxUpdates = MAX_UPDATES_PER_SLEEP;
    while (messages().size() > 0 && maxUpdates-- > 0) {
      var item = messages().poll();
      if (item != null) {
        if (!propertyPath.isEmpty()) {
          ObjectNode node = (ObjectNode) item;
          node.put(propertyName, item.at(propertyPath).asText());
        }
        for (var cmdName : commands) {
          var cmd = getCommand(cmdName);
          var params = getParams(cmd, item.toString());
          issueCommand(cmd, params);
        }
      }
    }
    super.runAction();
  }

  Map<String, String> getParams(IRainbowOperation operation, String jsonItem) {
    Map<String, String> params = new HashMap<>();
    var param0 = operation.getParameters()[0];
    if (param0.startsWith("$<jsonValue>")) {
      params.put(param0, jsonItem);
    }
    return params;
  }
}
