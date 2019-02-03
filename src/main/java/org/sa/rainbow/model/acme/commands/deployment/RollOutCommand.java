package org.sa.rainbow.model.acme.commands.deployment;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.SimpleCommand;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class RollOutCommand extends SimpleCommand {

  public RollOutCommand(
      AcmeModelInstance model, String target, String namespace, String deployment, String container, String image) {
    super("rollOut", model, target, namespace, deployment, container, image);
  }
}
