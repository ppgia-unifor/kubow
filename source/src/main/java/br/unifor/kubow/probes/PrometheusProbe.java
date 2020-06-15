package br.unifor.kubow.probes;

import br.unifor.kubow.services.prometheus.PrometheusClient;
import org.sa.rainbow.core.Rainbow;

import java.security.InvalidParameterException;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class PrometheusProbe extends KubowProbe {

  private final PrometheusClient client;
  private final String metricName;
  private final String namespace;
  private final String deploymentName;
  private final String promQl;

  /**
   * Constructs a probe to collect data from prometheus. The prometheus host is configured by `kubow.prometheus.url`
   * parameter defined in `rainbow.properties` file.
   * @param id The unique identifier
   * @param sleepTime The pause time
   * @param args An array that represents the namespace, the deployment`s name, the metric`s name, and the prometheus` query
   */
  public PrometheusProbe(String id, long sleepTime, String[] args) {
    super(id, probeType(args[0], args[1], args[2]), sleepTime, args);
    var host = Rainbow.instance().getProperty("kubow.prometheus.url");
    if (host == null) {
      throw new InvalidParameterException(
          "Property {kubow.prometheus.url} not defined in rainbow.properties file");
    }
    client = new PrometheusClient(host);
    namespace = args[0];
    deploymentName = args[1];
    metricName = args[2];
    promQl = args[3];
  }

  static String probeType(String namespace, String deployment, String metric) {
    return namespace.concat("-").concat(deployment).concat("-").concat(metric);
  }

  @Override
  protected Map<String, Object> collect() {
    var value = client.queryValue(promQl);
    if (value != null) {
      return of(metricName, value, "namespace", namespace, "name", deploymentName);
    }
    return null;
  }
}
