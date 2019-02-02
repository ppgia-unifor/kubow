package org.sa.rainbow.model.acme.commands.deployment;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.KubeAcmeModelCommand;

import java.util.List;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class ScaleDownCommand extends KubeAcmeModelCommand<IAcmeProperty> {

  public ScaleDownCommand(AcmeModelInstance model, String target, String... parameters) {
    super("scaleDown", model, target, parameters);
  }

  @Override
  protected List<IAcmeCommand<?>> doConstructCommand() throws RainbowModelException {
    return null;
  }

  @Override
  public IAcmeProperty getResult() throws IllegalStateException {
    return null;
  }
}
