package org.sa.rainbow.translator.probes;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.sa.rainbow.ApiClientFactory.defaultClient;

/**
 * A probe to collect data from a deployment and report as json
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class DeploymentInfoProbe extends KubeAbstractProbe {

  private static final Logger logger = LoggerFactory.getLogger(DeploymentInfoProbe.class);
  private final AppsV1Api appsV1Api;
  private final String namespace;
  private final String deploymentName;
  private final String selector;

  public DeploymentInfoProbe(String id, long sleepTime, String[] args) {
    super(
        id, "deployments." + args[0] + "." + args[1] + ".probe", sleepTime, args, defaultClient());
    appsV1Api = new AppsV1Api(apiClient());
    namespace = args[0];
    deploymentName = args[1];
    selector = args[2];
  }

  @Override
  protected Map<String, Object> collect() {
    var deployment = getDeployment();
    Map<String, Object> values = new HashMap<>();
    if (deployment.isPresent()) {
      var d = deployment.get();
      values.put("name", d.getMetadata().getName());
      values.put("namespace", d.getMetadata().getName());
      values.put("desiredReplicas", d.getSpec().getReplicas());
      values.put("currentReplicas", d.getStatus().getReplicas());
      values.put("availableReplicas", d.getStatus().getAvailableReplicas());
      values.put(
          "containers",
          d.getSpec().getTemplate().getSpec().getContainers().stream()
              .collect(toMap(V1Container::getName, V1Container::getImage)));
      values.put("labels", d.getMetadata().getLabels());
    }
    return values;
  }

  protected Optional<V1Deployment> getDeployment() {
    try {
      return of(appsV1Api.readNamespacedDeployment(deploymentName, namespace, null, false, false));
    } catch (ApiException ex) {
      logger.info(
          "An error occurred whe try to collect data from {}.{}", namespace, deploymentName);
      return empty();
    }
  }
}
