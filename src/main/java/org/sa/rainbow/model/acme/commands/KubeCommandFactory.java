package org.sa.rainbow.model.acme.commands;

import incubator.pval.Ensure;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.deployment.SetContainersCommand;
import org.sa.rainbow.model.acme.commands.deployment.SetDeploymentInfoCommand;
import org.sa.rainbow.model.acme.commands.deployment.SetDeploymentPropertyCommand;
import org.sa.rainbow.model.acme.commands.service.SetServicePropertyCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class KubeCommandFactory extends AcmeModelCommandFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(KubeCommandFactory.class);

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
    m_commandMap.put("setFidelity".toLowerCase(), SetFidelityCommand.class);
  }

  public SetFidelityCommand setFidelityCmd(IAcmeComponent znnService, int step) {
    Ensure.is_true (znnService.declaresType ("Service"));
    if (ModelHelper.getAcmeSystem (znnService) != m_modelInstance.getModelInstance ())
      throw new IllegalArgumentException (
              "Cannot create a command for a component that is not part of the system");
    return new SetFidelityCommand ((AcmeModelInstance) m_modelInstance, znnService.getQualifiedName(), String.valueOf(step));
  }
}
