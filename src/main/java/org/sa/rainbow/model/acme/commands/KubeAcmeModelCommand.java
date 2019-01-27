package org.sa.rainbow.model.acme.commands;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.AcmeModelOperation;

public abstract class KubeAcmeModelCommand<T> extends AcmeModelOperation<T> {

  public KubeAcmeModelCommand(
      String commandName, AcmeModelInstance model, String target, String... parameters) {
    super(commandName, model, target, parameters);
  }

  @Override
  protected boolean checkModelValidForCommand(IAcmeSystem model) {
    return model.declaresType("Kubernetes");
  }
}
