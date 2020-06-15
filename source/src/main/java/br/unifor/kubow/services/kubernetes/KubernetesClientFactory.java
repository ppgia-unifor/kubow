package br.unifor.kubow.services.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import org.sa.rainbow.core.Rainbow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubernetesClientFactory {

  private static Logger logger = LoggerFactory.getLogger(KubernetesClientFactory.class);
  private static ApiClient apiClient;

  public static ApiClient defaultClient() {

    try {
      apiClient = Config.defaultClient();

      var url = Rainbow.instance().getProperty("kubow.kubernetes.url", "");
      if (url != null && !url.isEmpty()) {
        apiClient.setBasePath(url);
      }

    } catch (IOException e) {
      logger.error("Error during the K8s client.", e);
    }
    return apiClient;
  }
}
