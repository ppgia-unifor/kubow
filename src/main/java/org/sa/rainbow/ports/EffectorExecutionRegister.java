package org.sa.rainbow.ports;

import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class EffectorExecutionRegister {

  private final Map<String, IEffectorExecutionPort> effectors;

  public EffectorExecutionRegister() {
    effectors = new HashMap<>();
  }

  public void register(IEffector effector, IEffectorExecutionPort executionPort) {
    if (effector != null && executionPort != null) {
      effectors.put(effector.id(), executionPort);
    }
  }

  public IEffectorExecutionPort find(IEffectorIdentifier identifier) {
    if (identifier != null) {
      return effectors.get(identifier.id());
    }
    return null;
  }
}
