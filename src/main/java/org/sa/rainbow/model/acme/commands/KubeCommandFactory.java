package org.sa.rainbow.model.acme.commands;

import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.deployment.SetContainersCommand;
import org.sa.rainbow.model.acme.commands.deployment.SetDeploymentInfoCommand;
import org.sa.rainbow.model.acme.commands.deployment.SetDeploymentPropertyCommand;
import org.sa.rainbow.model.acme.commands.service.SetServicePropertyCommand;

import java.io.InputStream;

public class KubeCommandFactory extends AcmeModelCommandFactory {

  public KubeCommandFactory(AcmeModelInstance modelInstance) {
    super(modelInstance);
  }

  public static KubeLoadModelCommand loadCommand(
      ModelsManager modelsManager, String modelName, InputStream stream, String source) {
    return new KubeLoadModelCommand(modelName, modelsManager, stream, source);
  }

  @Override
  protected void fillInCommandMap() {
    super.fillInCommandMap();
    m_commandMap.put("setContainers".toLowerCase(), SetContainersCommand.class);
    m_commandMap.put("setDeploymentInfo".toLowerCase(), SetDeploymentInfoCommand.class);
    m_commandMap.put("setDeploymentProperty".toLowerCase(), SetDeploymentPropertyCommand.class);
    m_commandMap.put("setServiceProperty".toLowerCase(), SetServicePropertyCommand.class);
  }
}
