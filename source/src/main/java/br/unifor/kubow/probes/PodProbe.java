package br.unifor.kubow.probes;

import br.unifor.kubow.services.kubernetes.KubernetesClientFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import io.kubernetes.client.openapi.ApiException;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Streams.stream;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class PodProbe extends KubernetesProbe {
  private static final Logger logger = LoggerFactory.getLogger(PodProbe.class);
  private static final String URL_TEMPLATE =
      "/apis/custom.metrics.k8s.io/v1beta1/namespaces/%s/pods/*/%s?selector=%s";
  private final String metricName;
  private final String metricAlias;
  private final String namespace;
  private final String deploymentName;
  private final String selector;

  public PodProbe(String id, long sleepTime, String[] args) {
    super(id, args[1] + "-" + args[4] + "-probe", sleepTime, args, KubernetesClientFactory.defaultClient());
    namespace = args[0];
    deploymentName = args[1];
    selector = args[2];
    metricName = args[3];
    metricAlias = args[4];
    logger.debug(
        "Created probe for metric {} for {}.{} with selector {}",
        metricAlias,
        namespace,
        deploymentName,
        selector);
  }

  @Override
  protected Map<String, Object> collect() {
    try {
      var request = buildGetRequest(buildUrl(selector));
      var response = apiClient().getHttpClient().newCall(request).execute();
      if (response.isSuccessful()) {
        var elements = objectReader().readTree(response.body().string()).get("items").elements();
        var avg = stream(elements).mapToDouble(this::getValue).average();
        if (avg.isPresent()) {
          return ImmutableMap.of(
              metricAlias, avg.getAsDouble(), "namespace", namespace, "name", deploymentName);
        }
      }
    } catch (IOException e) {
      logger.info(
          "An error occurred when try to collect data from {}.{} deployment. {}",
          namespace,
          deploymentName,
          e.getMessage());
    }

    return null;
  }

  Double getValue(JsonNode node) {
    var raw = node.get("value").asText();
    // The m represents milli-units, so for example, 901m means 901 milli-requests
    return Double.parseDouble(raw.replace("m", ""));
  }

  String buildUrl(String... selectors) {
    return format(URL_TEMPLATE, namespace, metricName, join("&", selectors));
  }

  Request buildGetRequest(String path) {
    try {
      return apiClient().buildRequest(
              path,
              "GET",
              emptyList(),
              emptyList(),
              null,
              Map.of(),
              Map.of(),
              Map.of(),
              new String[] {"BearerToken"},
              null);
    } catch (ApiException e) {
      logger.error("Cannot get from kubernetes api.", e);
      return null;
    }
  }
}
