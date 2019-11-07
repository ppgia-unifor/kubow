package br.unifor.kubow;

import io.prometheus.client.exporter.HTTPServer;
import br.unifor.kubow.services.api.Server;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static java.text.MessageFormat.format;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubowApplication {

  private static final Logger logger = LoggerFactory.getLogger(KubowApplication.class);

  private static boolean delegateStarted = false;
  private static boolean masterStarted = false;

  public static void main(String[] args) {
    Server.init();
    startConfig();

    var master = startMaster();
    if (master == null) {
      throw new KubowException("Cannot start Master component");
    }

    try {
      startDelegate();
    } catch (RainbowException e) {
      throw new KubowException("Cannot start Delegate component", e);
    }

    master.startProbes();
  }

  public static boolean isReady() {
    return delegateStarted && masterStarted;
  }

  static void startConfig() {
    var userDir = System.getenv("USER_DIR");
    if (userDir != null) {
      System.setProperty("user.dir", userDir);
    }

    var target = System.getenv("TARGET");
    var config = System.getenv("TARGET_PATH");

    if (target == null) {
      logger.warn("No TARGET configured. Set [default] as the target name");
      target = "default";
    }

    if (config == null) {
      var path = KubowApplication.class.getClassLoader().getResource(target);
      if (path == null) {
        var message = format("Target [{0}] does not exists in [{1}]", target, config);
        throw new RainbowAbortException(message);
      }
      config = Paths.get(path.getPath()).getParent().toString();
    }

    logger.info("Using target [{}] located in path [{}]", target, config);
    System.setProperty("rainbow.config", config);
    System.setProperty("rainbow.target", target);
  }

  static RainbowMaster startMaster() {
    try {
      logger.info("Initializing rainbow master");
      var master = new RainbowMaster();
      master.initialize();
      master.start();

      logger.info("Initialized rainbow master");
      masterStarted = true;
      return master;
    } catch (RainbowException e) {
      logger.error("Cannot start rainbow master.", e);
      return null;
    }
  }

  static void startDelegate() throws RainbowConnectionException {
    final int maxRetries = 5;
    for (int retries = 0; retries < maxRetries; retries++) {
      try {
        logger.info("Trying to initialize rainbow delegate");
        var delegate = new RainbowDelegate();
        delegate.initialize();
        delegate.start();
        Thread.sleep(5000); //waiting for delegate to be ready
        delegate.startProbes();
        logger.info("Initialized rainbow delegate with ID {}", delegate.getId());
        retries = maxRetries;
      } catch (RainbowException e) {
        if (retries < maxRetries) {
          logger.warn("Couldn't connect delegate. Will try {} times more", maxRetries - retries);
          continue;
        } else {
          throw e;
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    delegateStarted = true;
  }

  static void startPrometheus() throws IOException {
    var prometheusPort = System.getenv("PROMETHEUS_PORT");
    if (prometheusPort == null) {
      prometheusPort = "1002";
    }
    new HTTPServer(Integer.parseInt(prometheusPort));
  }
}
