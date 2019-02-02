package org.sa.rainbow.model.acme.commands.deployment;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.KubeAcmeModelCommand;

import java.util.List;

import static java.util.Collections.emptyList;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class RolloutCommand extends KubeAcmeModelCommand<IAcmeProperty> {

  public RolloutCommand(
      AcmeModelInstance model, String target, String deployment, String container, String image) {
    super("rollout", model, target, deployment, container, image);
  }

  @Override
  protected List<IAcmeCommand<?>> doConstructCommand() {
    return emptyList();
  }

  @Override
  public IAcmeProperty getResult() throws IllegalStateException {
    return null;
  }
}
