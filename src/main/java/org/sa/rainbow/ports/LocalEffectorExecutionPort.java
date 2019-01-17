package org.sa.rainbow.ports;

import org.sa.rainbow.core.ports.local.EffectorExecutionRegister;
import org.sa.rainbow.translator.effectors.IEffector;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;

import java.util.List;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class LocalEffectorExecutionPort implements IEffectorExecutionPort {

  private final IEffector m_effector;

  public LocalEffectorExecutionPort(EffectorExecutionRegister register, IEffector effector) {
    m_effector = effector;
    register.register(m_effector, this);
  }

  @Override
  public Outcome execute(List<String> args) {
    return m_effector.execute(args);
  }

  @Override
  public void dispose() {
    // nothing to dispose
  }
}
