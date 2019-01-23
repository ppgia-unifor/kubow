package org.sa.rainbow.translator.znn.gauges;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class DeploymentGauge extends AbstractJsonGaugeWithProbes {

  public static final String NAME = "G - Deployment Info Gauge";
  private final Set<String> commands;

  public DeploymentGauge(
      String id,
      long beaconPeriod,
      TypedAttribute gaugeDesc,
      TypedAttribute modelDesc,
      List<TypedAttributeWithValue> setupParams,
      Map<String, IRainbowOperation> mappings)
      throws RainbowException {
    super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);
    commands = mappings.keySet();
  }

  @Override
  protected void runAction() {
    int maxUpdates = MAX_UPDATES_PER_SLEEP;
    while (messages().size() > 0 && maxUpdates-- > 0) {
      var item = messages().poll();
      if (item != null) {
        String jsonItem = item.toString();
        for (var cmdName : commands) {
          var cmd = getCommand(cmdName);
          Map<String, String> params = new HashMap<>();
          params.put(cmd.getParameters()[0], jsonItem);
          issueCommand(cmd, params);
        }
      }
    }
    super.runAction();
  }
}
