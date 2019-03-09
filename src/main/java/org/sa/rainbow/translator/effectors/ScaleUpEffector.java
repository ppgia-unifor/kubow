package org.sa.rainbow.translator.effectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.sa.rainbow.monitor.EffectorMonitor;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ScaleUpEffector extends KubeEffector {

  public ScaleUpEffector(String refID, String name) {
    super(refID, name);
  }

  @Override
  protected Outcome internalExecute(List<String> args) {
    var namespace = args.get(0);
    var deployment = args.get(1);
    var desiredReplicas = Integer.parseInt(args.get(2));
    var timer = EffectorMonitor.latency().labels("scaleUp", namespace, deployment).startTimer();

    try (var client = new DefaultKubernetesClient()) {
      var response =
          client
              .apps()
              .deployments()
              .inNamespace(namespace)
              .withName(deployment)
              .scale(desiredReplicas, true);

      if (desiredReplicas == response.getStatus().getReplicas()) {
        return Outcome.SUCCESS;
      } else {
        return Outcome.FAILURE;
      }
    } catch (Exception ex) {
      return Outcome.FAILURE;
    } finally {
      timer.observeDuration();
    }
  }
}
