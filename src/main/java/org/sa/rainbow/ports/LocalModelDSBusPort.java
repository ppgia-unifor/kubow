package org.sa.rainbow.ports;

import incubator.pval.Ensure;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class LocalModelDSBusPort implements IModelDSBusPublisherPort, IModelDSBusSubscriberPort {

  private IModelDSBusPublisherPort m_callback;

  public LocalModelDSBusPort() {
  }

  @Override
  public OperationResult publishOperation(IRainbowOperation cmd) {
    Ensure.not_null(cmd);
    return m_callback.publishOperation(cmd);
  }

  @Override
  public void unsubscribeToOperations(IModelDSBusPublisherPort callback) {
    if (callback.equals(m_callback)) {
      m_callback = null;
    }
  }

  @Override
  public void subscribeToOperations(IModelDSBusPublisherPort callback) {
    m_callback = callback;
  }

  @Override
  public void dispose() {

  }

  @Override
  public IRainbowMessage createMessage() {
    return null;
  }
}
