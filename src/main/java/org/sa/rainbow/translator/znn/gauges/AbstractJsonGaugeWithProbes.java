package org.sa.rainbow.translator.znn.gauges;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.gauges.AbstractGaugeWithProbes;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Listen for probes that produces json as their data format.
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class AbstractJsonGaugeWithProbes extends AbstractGaugeWithProbes {

  private final Queue<JsonNode> messages;
  private final Logger logger = LoggerFactory.getLogger(AbstractJsonGaugeWithProbes.class);

  protected AbstractJsonGaugeWithProbes(
      String threadName,
      String id,
      long beaconPeriod,
      TypedAttribute gaugeDesc,
      TypedAttribute modelDesc,
      List<TypedAttributeWithValue> setupParams,
      Map<String, IRainbowOperation> mappings)
      throws RainbowException {
    super(threadName, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    messages = new LinkedList<>();
  }

  @Override
  public void reportFromProbe(IProbeIdentifier probe, String data) {
    var objectMapper = new ObjectMapper();
    try {
      messages.add(objectMapper.readTree(data));
    } catch (IOException e) {
      logger.error(String.format("Impossible to parse json object %s", data), e);
    }
  }

  public Queue<JsonNode> messages() {
    return messages;
  }
}
