package br.unifor.kubow.services.kubernetes;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubernetesClientFactory {

  private static Logger logger = LoggerFactory.getLogger(KubernetesClientFactory.class);

  public static ApiClient defaultClient() {
    ApiClient apiClient = null;
    try {
      apiClient = Config.defaultClient();
      logger.info(
          "Available authentication {}", String.join(",", apiClient.getAuthentications().keySet()));
    } catch (IOException e) {
      logger.error("Error during the K8s client.", e);
    }
    return apiClient;
  }
}
