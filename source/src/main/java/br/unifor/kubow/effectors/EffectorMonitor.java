package br.unifor.kubow.effectors;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class EffectorMonitor {

  private static final Summary effectorLatency =
      Summary.build()
          .name("rainbow_effector_latency_seconds")
          .help("Effector latency in seconds.")
          .labelNames("name", "namespace", "resource")
          .register();

  private static final Counter counter =
      Counter.build().name("rainbow_effector_total").help("Total effector executions.").labelNames("name", "outcome").register();

  public static Summary latency() {
    return effectorLatency;
  }

  public static Counter counter() {
    return counter;
  }
}
