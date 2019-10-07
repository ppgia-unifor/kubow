package br.unifor.kubow.services.prometheus;

import br.unifor.kubow.KubowException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class PrometheusResponse {
  private final JsonNode data;

  public PrometheusResponse(JsonNode data) {
    this.data = data;
  }

  public boolean isSuccessful() {
    return false;
  }

  public Object value() {
    var resultType = data.get("resultType").asText();

    if (resultType.equals("vector")) {
      var result = data.get("result");
      if (result != null && result.elements().hasNext()) {
        var metric = result.elements().next();
        var value = metric.get("value").get(1).asDouble();
        return value;
      }
    } else {
      throw new KubowException("Invalid response type");
    }
    return null;
  }
}
