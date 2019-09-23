package org.sa.rainbow.ports;

import edu.cmu.cs.able.eseb.rpc.OperationTimedOutException;
import org.sa.rainbow.core.ports.eseb.rpc.IESEBEffectorExecutionRemoteInterface;
import org.sa.rainbow.core.ports.local.EffectorExecutionRegister;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import java.util.List;

public class ESEBEffectorExecutionRequirerPort implements IESEBEffectorExecutionRemoteInterface {

  private final IEffectorExecutionPort m_stub;

  public ESEBEffectorExecutionRequirerPort(
      EffectorExecutionRegister register, IEffectorIdentifier effector) {
    m_stub = register.find(effector);
  }

  @Override
  public Outcome execute(List<String> args) {
    try {
      return m_stub.execute(args);
    } catch (OperationTimedOutException e) {
      return Outcome.TIMEOUT;
    }
  }

  @Override
  public void dispose() {}
}
