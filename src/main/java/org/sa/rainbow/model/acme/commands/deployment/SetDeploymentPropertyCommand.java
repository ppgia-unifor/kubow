package org.sa.rainbow.model.acme.commands.deployment;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.model.acme.commands.KubePropertyCommand;

import java.io.IOException;

/**
 * Set the property value {@param propertyName} in the Deployment {@param target}
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class SetDeploymentPropertyCommand extends KubePropertyCommand {

  public SetDeploymentPropertyCommand(
      AcmeModelInstance model, String target, String params, String propertyName)
      throws IOException {
    super("Deployment", propertyName, model, target, params);
  }
}
