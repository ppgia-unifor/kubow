package org.sa.rainbow.model.acme.commands.deployment;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.SimpleCommand;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ScaleUpCommand extends SimpleCommand {

  public ScaleUpCommand(AcmeModelInstance model, String target, String namespace, String deployment, int desiredReplicas) {
    super("scaleUp", model, target, namespace, deployment, String.valueOf(desiredReplicas));
  }
}
