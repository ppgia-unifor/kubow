package org.sa.rainbow.translator.effectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ScaleUpEffector extends AbstractEffector {

  public ScaleUpEffector(String refID, String name) {
    super(refID, name, Kind.JAVA);
  }

  @Override
  public Outcome execute(List<String> args) {
    var namespace = args.get(0);
    var deployment = args.get(1);
    var desiredReplicas = Integer.parseInt(args.get(2));
//    var count = Integer.parseInt(args.get(2));

    try (var client = new DefaultKubernetesClient()) {

//      var replicas =
//          client
//              .apps()
//              .deployments()
//              .inNamespace(namespace)
//              .withName(deployment)
//              .get()
//              .getStatus()
//              .getReplicas();

//      var desiredReplicas = replicas + count;
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
      ex.printStackTrace();
      return Outcome.FAILURE;
    }
  }
}
