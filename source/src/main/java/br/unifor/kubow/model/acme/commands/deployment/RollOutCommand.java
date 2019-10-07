package br.unifor.kubow.model.acme.commands.deployment;

import io.prometheus.client.Counter;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.SimpleCommand;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class RollOutCommand extends SimpleCommand {

  private static final Counter counter =
      Counter.build()
          .name("rainbow_command_rollout_total")
          .help("Total executions of command rollout.")
          .labelNames("namespace", "deployment", "container", "image")
          .register();

  public RollOutCommand(
      AcmeModelInstance model,
      String target,
      String namespace,
      String deployment,
      String container,
      String image) {
    super("rollOut", model, target, namespace, deployment, container, image);
    counter.labels(namespace, deployment, container, image).inc();
  }
}
