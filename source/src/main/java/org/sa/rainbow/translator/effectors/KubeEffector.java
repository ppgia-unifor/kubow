package org.sa.rainbow.translator.effectors;

import org.sa.rainbow.monitor.EffectorMonitor;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public abstract class KubeEffector extends AbstractEffector {

  private final String name;

  protected KubeEffector(String refID, String name) {
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
