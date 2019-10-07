package br.unifor.kubow.model.acme.commands.service;

import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.KubowPropertyCommand;

import java.io.IOException;

/**
 * Set the property value {@param propertyName} in the Service {@param target}
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 * */
public class SetServicePropertyCommand extends KubowPropertyCommand {

  public SetServicePropertyCommand(
      AcmeModelInstance model, String target, String params, String propertyName)
      throws IOException {
    super("ServiceT", propertyName, model, target, params);
  }
}
