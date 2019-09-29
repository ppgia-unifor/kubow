package org.sa.rainbow.translator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.InvalidParameterException;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class PrometheusClient {

  private static final Logger logger = LoggerFactory.getLogger(PrometheusClient.class);
  private static final String PATH_QUERY = "/api/v1/query";
  private final OkHttpClient client;
  private final ObjectMapper objectMapper;
  private final String host;

  public PrometheusClient(String host) {
    this.host = host;
    client = new OkHttpClient();
    objectMapper = new ObjectMapper();
    logger.info("Prometheus client successfully initialized for host {}", host);
  }

  public Object queryValue(String promQl) {
    try {
      var url = host.concat(PATH_QUERY).concat("?query=").concat(promQl);
      var request = new Request.Builder().url(url).build();
      var response = client.newCall(request).execute();

      if (response.isSuccessful()) {
        var body = objectMapper.readTree(response.body().string());
        var data = body.get("data");

        if (data != null) {
          var resultType = data.get("resultType").asText();
          switch (resultType) {
            case "vector":
              var result = data.get("result");
              if (result != null && result.elements().hasNext()) {
                var metric = result.elements().next();
                var value = metric.get("value").get(1).asDouble();
                logger.info("Prometheus query {} returning value {}", promQl, value);
                return value;
              }
              logger.warn("Prometheus query {} returning value null", promQl);
              return null;
            case "matrix":
            case "scalar":
            case "string":
            default:
              logger.warn("Not supported resultType {}", body.get("data").get("resultType"));
          }
        }
      }
    } catch (IOException | InvalidParameterException e) {
      logger.error("Was not possible to execute query in prometheus. Reason: {}", e.getMessage(), e);
    }
    return null;
  }
}
