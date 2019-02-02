package org.sa.rainbow.model.acme.commands;

import incubator.pval.Ensure;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.deployment.*;
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

  private void registerCommand(String name, Class clazz) {
    m_commandMap.put(name.toLowerCase(), clazz);
  }

  @Override
  protected void fillInCommandMap() {
    super.fillInCommandMap();
    registerCommand("setContainers", SetContainersCommand.class);
    registerCommand("setDeploymentInfo", SetDeploymentInfoCommand.class);
    registerCommand("setDeploymentProperty", SetDeploymentPropertyCommand.class);
    registerCommand("setServiceProperty", SetServicePropertyCommand.class);
    registerCommand("rollout", RolloutCommand.class);
    registerCommand("scaleUp", ScaleUpCommand.class);
    registerCommand("scaleDown", ScaleDownCommand.class);
  }

  private void isDeployment(IAcmeComponent deployment) {
    Ensure.is_true(deployment.declaresType("Deployment"));
    if (ModelHelper.getAcmeSystem(deployment) != m_modelInstance.getModelInstance()) {
      throw new IllegalArgumentException(
          "Cannot create a command for a component that is not part of the system");
    }
  }

  public RolloutCommand rolloutCmd(IAcmeComponent component, String container, String image) {
    isDeployment(component);
    return new RolloutCommand(
        (AcmeModelInstance) m_modelInstance, component.getQualifiedName(), component.getName(), container, image);
  }

  public ScaleUpCommand scaleUpCmd(IAcmeComponent component, int replica) {
    isDeployment(component);
    return new ScaleUpCommand(
        (AcmeModelInstance) m_modelInstance, component.getQualifiedName(), String.valueOf(replica));
  }

  public ScaleDownCommand scaleDownCmd(IAcmeComponent component, int replica) {
    isDeployment(component);
    return new ScaleDownCommand(
        (AcmeModelInstance) m_modelInstance, component.getQualifiedName(), String.valueOf(replica));
  }
}
