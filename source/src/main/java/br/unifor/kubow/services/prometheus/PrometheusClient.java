package br.unifor.kubow.services.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
        var promResponse = new PrometheusResponse(body.get("data"));
        var value = promResponse.value();
        logger.debug("Prometheus query {} returning value {}", promQl, value);
        return value;
      } else {
        logger.error("Prometheus sent a error request. {}", response.body().string());
      }
    } catch (IOException | InvalidParameterException e) {
      logger.error("Was not possible to execute query in prometheus. Reason: {}", e.getMessage(), e);
    }
    return null;
  }
}
