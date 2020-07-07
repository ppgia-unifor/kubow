package br.unifor.kubow.model.acme;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import br.unifor.kubow.model.acme.commands.KubowCommandFactory;

public class KubowModelUpdateOperatorsImpl extends AcmeModelInstance {

  private KubowCommandFactory m_commandFactory;

  public KubowModelUpdateOperatorsImpl(IAcmeSystem system, String source) {
    super(system, source);
    // Make sure it is the right family
  }

  @Override
  public KubowCommandFactory getCommandFactory() {
    if (m_commandFactory == null) {
      m_commandFactory = new KubowCommandFactory(this);
    }
    return m_commandFactory;
  }

  @Override
  protected AcmeModelInstance generateInstance(IAcmeSystem sys) {
    return new KubowModelUpdateOperatorsImpl(sys, getOriginalSource());
  }
}
