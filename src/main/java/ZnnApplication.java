import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.gui.RainbowGUI;

import java.nio.file.Paths;

/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class ZnnApplication {

  public static void main(String[] args) {
    try {

      String target = "default";
      String config = Paths.get(ZnnApplication.class.getClassLoader().getResource(target).getPath()).getParent().toString();

      System.setProperty("rainbow.config", config);
      System.setProperty("rainbow.target", target);

      RainbowMaster master = new RainbowMaster();
      master.initialize();
      master.start();

      RainbowDelegate delegate = new RainbowDelegate();
      delegate.initialize();
      delegate.start();

      RainbowGUI gui = new RainbowGUI(master);
//      gui.display();
    } catch (RainbowException e) {
      e.printStackTrace();
    }
  }
}
