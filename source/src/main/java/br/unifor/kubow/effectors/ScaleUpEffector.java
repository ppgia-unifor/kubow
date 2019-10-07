package br.unifor.kubow.effectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ScaleUpEffector extends KubowEffector {

  public ScaleUpEffector(String refID, String name) {
    super(refID, name);
  }

  @Override
  protected IEffectorExecutionPort.Outcome internalExecute(List<String> args) {
    var namespace = args.get(0);
    var deployment = args.get(1);
    var count = Integer.parseInt(args.get(2));
    var timer = EffectorMonitor.latency().labels("scaleUp", namespace, deployment).startTimer();

    try (var client = new DefaultKubernetesClient()) {
      var replicas =
              client
                      .apps()
                      .deployments()
                      .inNamespace(namespace)
                      .withName(deployment)
                      .get()
                      .getStatus()
                      .getReplicas();
      var desiredReplicas = replicas + count;
      var response =
          client
              .apps()
              .deployments()
              .inNamespace(namespace)
              .withName(deployment)
              .scale(desiredReplicas, true);

      if (desiredReplicas == response.getStatus().getReplicas()) {
        return IEffectorExecutionPort.Outcome.SUCCESS;
      } else {
        return IEffectorExecutionPort.Outcome.FAILURE;
      }
    } catch (Exception ex) {
      return IEffectorExecutionPort.Outcome.FAILURE;
    } finally {
      timer.observeDuration();
    }
  }
}
