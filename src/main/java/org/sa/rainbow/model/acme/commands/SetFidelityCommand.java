package org.sa.rainbow.model.acme.commands;

import org.acmestudio.acme.element.property.IAcmeProperty;
import org.acmestudio.acme.model.command.IAcmeCommand;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.model.acme.AcmeModelInstance;

import java.util.List;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class SetFidelityCommand extends KubeAcmeModelCommand<IAcmeProperty> {

  public SetFidelityCommand(AcmeModelInstance model, String target, String... parameters) {
    super("setFidelity", model, target, parameters);
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
