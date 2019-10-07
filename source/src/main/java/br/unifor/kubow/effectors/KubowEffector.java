package br.unifor.kubow.effectors;

import org.sa.rainbow.translator.effectors.AbstractEffector;

import java.util.List;

/**
 * A template for kubow' effectors
 *
 * @author Carlos Mendes (cmendesce@gmail.com) */
public abstract class KubowEffector extends AbstractEffector {

  private final String name;

  protected KubowEffector(String refID, String name) {
    super(refID, name, Kind.JAVA);
    this.name = name;
  }

  protected abstract Outcome internalExecute(List<String> args);

  @Override
  public Outcome execute(List<String> args) {
    var result = internalExecute(args);
    EffectorMonitor.counter().labels(name, result.name()).inc();
    return result;
  }
}
