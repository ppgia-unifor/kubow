package br.unifor.kubow.gauges;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubowGauge extends AbstractGaugeWithProbes {

  private static final String NAME = "G - Kubow Gauge";

  private final Logger logger = LoggerFactory.getLogger(KubowGauge.class);
  private final Set<String> commands;
  private final Queue<JsonNode> messages;
  private final ObjectReader objectReader;
  private String propertyPath = "";
  private String propertyName = "";

  public KubowGauge(
      String id,
      long beaconPeriod,
      TypedAttribute gaugeDesc,
      TypedAttribute modelDesc,
      List<TypedAttributeWithValue> setupParams,
      Map<String, IRainbowOperation> mappings)
      throws RainbowException {
    super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    messages = new LinkedList<>();
    objectReader = new ObjectMapper().readerFor(String.class);
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
    while (!messages.isEmpty() && maxUpdates-- > 0) {
      var item = messages.poll();
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

  @Override
  public void reportFromProbe(IProbeIdentifier probe, String data) {
    try {
      messages.add(objectReader.readTree(data));
    } catch (IOException e) {
      logger.error(String.format("Impossible to parse json object %s", data), e);
    }
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
