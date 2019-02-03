package org.sa.rainbow.model.acme.commands;

import incubator.pval.Ensure;
import org.acmestudio.acme.ModelHelper;
import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.model.acme.AcmeModelCommandFactory;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.deployment.*;
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

  private void registerCommand(String name, Class clazz) {
    m_commandMap.put(name, clazz);
  }

  @Override
  protected void fillInCommandMap() {
    super.fillInCommandMap();
    registerCommand("setContainers".toLowerCase(), SetContainersCommand.class);
    registerCommand("setDeploymentInfo".toLowerCase(), SetDeploymentInfoCommand.class);
    registerCommand("setDeploymentProperty".toLowerCase(), SetDeploymentPropertyCommand.class);
    registerCommand("setServiceProperty".toLowerCase(), SetServicePropertyCommand.class);
    registerCommand("rollOut", RollOutCommand.class);
    registerCommand("scaleUp", ScaleUpCommand.class);
    registerCommand("scaleDown", ScaleDownCommand.class);
    registerCommand("logger", ScaleDownCommand.class);
  }

  private void isDeployment(IAcmeComponent deployment) {
    Ensure.is_true(deployment.declaresType("Deployment"));
    if (ModelHelper.getAcmeSystem(deployment) != m_modelInstance.getModelInstance()) {
      throw new IllegalArgumentException(
          "Cannot create a command for a component that is not part of the system");
    }
  }

  private String getNamespace(IAcmeComponent component) throws RainbowModelException {
    var prop = component.getProperty("namespace");
    if (prop == null) {
      throw new RainbowModelException(
          "Property namespace not found in component " + component.getName());
    }
    var value = prop.getValue();
    return PropertyHelper.toJavaVal(value).toString();
  }

  public RollOutCommand rollOutCmd(IAcmeComponent component, String container, String image)
      throws RainbowModelException {
    isDeployment(component);

    return new RollOutCommand(
        (AcmeModelInstance) m_modelInstance,
        component.getQualifiedName(),
        getNamespace(component),
        component.getName(),
        container,
        image);
  }

  public ScaleUpCommand scaleUpCmd(IAcmeComponent component, int desiredReplicas)
      throws RainbowModelException {
    isDeployment(component);
    return new ScaleUpCommand(
        (AcmeModelInstance) m_modelInstance,
        component.getQualifiedName(),
        getNamespace(component),
        component.getName(),
        desiredReplicas);
  }

  public ScaleDownCommand scaleDownCmd(IAcmeComponent component, int desiredReplicas)
      throws RainbowModelException {
    isDeployment(component);
    return new ScaleDownCommand(
        (AcmeModelInstance) m_modelInstance,
        component.getQualifiedName(),
        getNamespace(component),
        component.getName(),
        desiredReplicas);
  }

  public LoggerCommand loggerCmd(IAcmeComponent component, String... params) {
    return new LoggerCommand(
        (AcmeModelInstance) m_modelInstance, component.getQualifiedName(), params);
  }
}
