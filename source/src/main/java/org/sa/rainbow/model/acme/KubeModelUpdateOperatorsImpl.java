package org.sa.rainbow.model.acme;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.commands.KubeCommandFactory;

public class KubeModelUpdateOperatorsImpl extends AcmeModelInstance {

  private KubeCommandFactory m_commandFactory;

  public KubeModelUpdateOperatorsImpl(IAcmeSystem system, String source) {
    super(system, source);
    // Make sure it is the right family
  }

  @Override
  public KubeCommandFactory getCommandFactory() {
    if (m_commandFactory == null) {
      m_commandFactory = new KubeCommandFactory(this);
    }
    return m_commandFactory;
  }

  @Override
  protected AcmeModelInstance generateInstance(IAcmeSystem sys) {
    return new KubeModelUpdateOperatorsImpl(sys, getOriginalSource());
  }
}
