package org.sa.rainbow.translator.probes;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.kubernetes.client.ApiClient;

/**
 * Base probe to get values from kubernetes API
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public abstract class KubeAbstractProbe extends AbstractRunnableJsonProbe {

  private final ApiClient apiClient;
  private final KubernetesClient kubernetesClient;

  public KubeAbstractProbe(
      String id, String type, long sleepTime, String[] args, ApiClient apiClient) {
    super(id, type, sleepTime, args);
    this.apiClient = apiClient;
    kubernetesClient = new DefaultKubernetesClient();
  }

  KubernetesClient kubernetesClient() {
    return kubernetesClient;
  }

  ApiClient apiClient() {
    return apiClient;
  }
}
