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
    ApiClient apiClient = null;
    try {
      apiClient = Config.defaultClient();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return apiClient;
  }
}
