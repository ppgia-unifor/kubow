package org.sa.rainbow.translator.effectors;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.sa.rainbow.monitor.EffectorMonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class UpdateConfigEffector extends KubeEffector {

  public UpdateConfigEffector(String refID, String name) {
    super(refID, name);
  }

  @Override
  protected Outcome internalExecute(List<String> args) {
    var namespace = args.get(0);
    var configMap = args.get(1);
    var rawValues = args.get(2);

    if (rawValues == null) {
      throw new IllegalArgumentException("Parameter values in effector is null");
    }
    var timer =
        EffectorMonitor.latency().labels("updateConfigMap", namespace, configMap).startTimer();

    try (var client = new DefaultKubernetesClient()) {
      Map<String, String> values = new HashMap<>();
      for (var item : rawValues.split(";")) {
        var parts = item.split("=");
        values.put(parts[0], parts[1]);
      }
      client.configMaps().inNamespace(namespace).withName(configMap).edit().withData(values).done();
      return Outcome.SUCCESS;
    } catch (Exception ex) {
      return Outcome.FAILURE;
    } finally {
      timer.observeDuration();
    }
  }
}
