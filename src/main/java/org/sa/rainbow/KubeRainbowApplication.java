package org.sa.rainbow;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class KubeRainbowApplication {

  private static final Logger logger = LoggerFactory.getLogger(KubeRainbowApplication.class);

  public static void main(String[] args) {
    try {
      String target = "znn";
      String config =
          Paths.get(KubeRainbowApplication.class.getClassLoader().getResource(target).getPath())
              .getParent()
              .toString();

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
