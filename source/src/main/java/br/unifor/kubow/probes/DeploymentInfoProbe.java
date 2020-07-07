package br.unifor.kubow.probes;

import br.unifor.kubow.services.kubernetes.KubernetesClientFactory;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentCondition;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toMap;
import static org.joda.time.Seconds.secondsBetween;

/**
 * A probe to collect data from a deployment and report as json
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class DeploymentInfoProbe extends KubernetesProbe {

  private static final Logger logger = LoggerFactory.getLogger(DeploymentInfoProbe.class);
  private final AppsV1Api appsV1Api;
  private final String namespace;
  private final String deploymentName;

  public DeploymentInfoProbe(String id, long sleepTime, String[] args) {
    super(id, "deployments." + args[0] + "." + args[1] + ".probe", sleepTime, args, KubernetesClientFactory.defaultClient());
    appsV1Api = new AppsV1Api(apiClient());
    namespace = args[0];
    deploymentName = args[1];
  }

  @Override
  protected Map<String, Object> collect() {
    var deployment = getDeployment();
    if (deployment.isPresent()) {
      var values = new HashMap<String, Object>();
      var d = deployment.get();
      values.put("name", d.getMetadata().getName());
      values.put("namespace", d.getMetadata().getNamespace());
      values.put("desiredReplicas", d.getSpec().getReplicas());
      values.put("currentReplicas", d.getStatus().getReplicas());
      values.put("availableReplicas", d.getStatus().getAvailableReplicas());
      values.put("containers", extractContainers(d));
      var latest = extractLatestStatus(d);
      values.put("status", latest.getType());
      values.put("lastUpdate", secondsBetween(latest.getLastUpdateTime(), DateTime.now()).getSeconds());
      return values;
    }
    return null;
  }

  public static V1DeploymentCondition extractLatestStatus(V1Deployment d) {
    var conditions = d.getStatus().getConditions();
    conditions.sort(comparing(V1DeploymentCondition::getLastUpdateTime));
    return conditions.get(conditions.size() - 1);
  }

  private Map<String, String> extractContainers(V1Deployment deployment) {
    return deployment.getSpec().getTemplate().getSpec().getContainers().stream()
        .collect(toMap(V1Container::getName, V1Container::getImage));
  }

  protected Optional<V1Deployment> getDeployment() {
    try {
      return of(appsV1Api.readNamespacedDeployment(deploymentName, namespace, null, false, false));
    } catch (ApiException ex) {
      logger.error(
          "An error occurred when trying to collect data from {}.{}. Response Body: {}", namespace, deploymentName, ex.getResponseBody(), ex);
      return empty();
    }
  }
}
