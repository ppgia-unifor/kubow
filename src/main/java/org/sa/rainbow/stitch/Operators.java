package org.sa.rainbow.stitch;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.basicmodel.core.AcmeRecordValue;
import org.acmestudio.basicmodel.core.AcmeSequenceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class Operators {

  private static final Logger logger = LoggerFactory.getLogger(Operators.class);

  private Operators() {}

  public static boolean containerImage(IAcmeComponent deployment, String container, String image) {
    var containers = (AcmeSequenceValue) deployment.getProperty("containers").getValue();

    if (containers != null) {
      var values = (List<AcmeRecordValue>) containers.getValues();
      var imageName = PropertyHelper.toAcmeVal(image);
      return values.stream()
          .anyMatch((AcmeRecordValue p) -> p.getField("image").getValue().equals(imageName));
    } else {
      logger.warn("Property containers of {} is null", deployment.getName());
    }
    return false;
  }
}
