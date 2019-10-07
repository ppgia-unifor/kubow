package br.unifor.kubow.model.acme.commands.deployment;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.KubowPropertyCommand;

import java.io.IOException;

/**
 * Set the property value {@param propertyName} in the Deployment {@param target}
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class SetDeploymentPropertyCommand extends KubowPropertyCommand {

  public SetDeploymentPropertyCommand(
      AcmeModelInstance model, String target, String params, String propertyName)
      throws IOException {
    super("DeploymentT", propertyName, model, target, params);
  }
}
