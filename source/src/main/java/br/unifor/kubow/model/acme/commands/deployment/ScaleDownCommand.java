package br.unifor.kubow.model.acme.commands.deployment;

import io.prometheus.client.Counter;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.SimpleCommand;

import static java.lang.String.valueOf;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ScaleDownCommand extends SimpleCommand {

  private static final Counter counter =
      Counter.build()
          .name("rainbow_command_scaledown_total")
          .help("Total executions of command scaleDown.")
          .labelNames("namespace", "deployment", "desiredReplicas")
          .register();

  public ScaleDownCommand(
      AcmeModelInstance model,
      String target,
      String namespace,
      String deployment,
      int desiredReplicas) {
    super("scaleDown", model, target, namespace, deployment, valueOf(desiredReplicas));
    counter.labels(namespace, deployment, valueOf(desiredReplicas));
  }
}
