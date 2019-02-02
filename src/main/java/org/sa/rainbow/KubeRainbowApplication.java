package org.sa.rainbow;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowAbortException;
import org.sa.rainbow.core.error.RainbowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

import static java.text.MessageFormat.format;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubeRainbowApplication {

  private static final Logger logger = LoggerFactory.getLogger(KubeRainbowApplication.class);

  public static void main(String[] args) {
    try {

      var target = System.getenv("TARGET");
      var config = System.getenv("TARGET_PATH");

      if (target == null) {
        logger.warn("No TARGET configured. Using the built in target");
        target = "default";
      }

      if (config == null) {
        var path = KubeRainbowApplication.class.getClassLoader().getResource(target);
        if (path == null) {
          var message =
              format(
                  "Target [{0}] does not exists in [{1}]",
                  target, KubeRainbowApplication.class.getClassLoader().getResource(".").getPath());
          throw new RainbowAbortException(message);
        }
        config = Paths.get(path.getPath()).getParent().toString();
      }

      logger.info("Using target [{}] located in path [{}]", target, config);
      System.setProperty("rainbow.config", config);
      System.setProperty("rainbow.target", target);

      RainbowMaster master = new RainbowMaster();
      master.initialize();
      master.start();

      RainbowDelegate delegate = new RainbowDelegate();
      delegate.initialize();
      delegate.start();
    } catch (RainbowException e) {
      logger.error("Cannot start rainbow.", e);
    }
  }
}
