package org.sa.rainbow.model.acme.commands;

import org.sa.rainbow.model.acme.AcmeModelInstance;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class LoggerCommand extends SimpleCommand {

  public LoggerCommand(AcmeModelInstance model, String target, String... parameters) {
    super("logger", model, target, parameters);
  }
}
