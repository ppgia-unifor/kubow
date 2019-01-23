package org.sa.rainbow;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class ApiClientFactory {

  private static Logger logger = LoggerFactory.getLogger(ApiClientFactory.class);

  public static ApiClient defaultClient() {
    try {
      var apiClient = Config.defaultClient();
      logger.debug("K8s api client initialized: " + apiClient.getBasePath());
      return apiClient;
    } catch (IOException e) {
      logger.error("Could not initialize the K8s api client. Reason: " + e.getMessage(), e);
      return null;
    }
  }
}
