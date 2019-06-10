package org.sa.rainbow;

import io.prometheus.client.exporter.HTTPServer;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.error.RainbowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static java.text.MessageFormat.format;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubeRainbowApplication {

  private static final Logger logger = LoggerFactory.getLogger(KubeRainbowApplication.class);

  public static void main(String[] args) throws IOException {
    try {

      var target = System.getenv("TARGET");
      var config = System.getenv("TARGET_PATH");

      if (target == null) {
        logger.warn("No TARGET configured. Set [default] as the target name");
        target = "default";
      }

      if (config == null) {
        var path = KubeRainbowApplication.class.getClassLoader().getResource(target);
        if (path == null) {
          var message = format("Target [{0}] does not exists in [{1}]", target, config);
          throw new RainbowAbortException(message);
        }
        config = Paths.get(path.getPath()).getParent().toString();
      }

      logger.info("Using target [{}] located in path [{}]", target, config);
      System.setProperty("rainbow.config", config);
      System.setProperty("rainbow.target", target);

      startPrometheus();

      RainbowMaster master = new RainbowMaster();
      master.initialize();
      master.start();

      var waitingTime = System.getenv("RM_WAITING_TIME");
      if (waitingTime == null) {
        waitingTime = "3000";
      }
      logger.info("Waiting for {}ms to master to be ready", waitingTime);
      Thread.sleep(Integer.parseInt(waitingTime));
      logger.info("Initializing rainbow delegate");
      RainbowDelegate delegate = new RainbowDelegate();
      delegate.initialize();
      delegate.start();
    } catch (RainbowException e) {
      logger.error("Cannot start rainbow.", e);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  static void startPrometheus() throws IOException {
    var prometheusPort = System.getenv("PROMETHEUS_PORT");
    if (prometheusPort == null) {
      prometheusPort = "1002";
    }
    new HTTPServer(Integer.parseInt(prometheusPort));
  }
}
