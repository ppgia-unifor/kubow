package org.sa.rainbow.model.acme.commands;

import io.prometheus.client.Counter;
import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class UpdateConfigCommand extends SimpleCommand {

  private static final Counter counter =
          Counter.build()
                  .name("rainbow_command_updateconfig_total")
                  .help("Total executions of command updateConfig.")
                  .labelNames("namespace", "configmap", "value")
                  .register();

  protected UpdateConfigCommand(
    AcmeModelInstance model,
    String target,
    String namespace,
    String configmap,
    String values) {
      super("updateConfig", model, target, namespace, configmap, values);
      counter.labels(namespace, configmap, values).inc();
  }
}
