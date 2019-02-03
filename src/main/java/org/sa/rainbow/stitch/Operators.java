package org.sa.rainbow.stitch;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.basicmodel.core.AcmeRecordValue;
import org.acmestudio.basicmodel.core.AcmeSequenceValue;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class Operators {

  public static boolean containerImage(IAcmeComponent deployment, String container, String image) {
    var containers = (AcmeSequenceValue) deployment.getProperty("containers").getValue();
    var imageName = PropertyHelper.toAcmeVal(image);
    var values = ((List<AcmeRecordValue>) containers.getValues());

    return values.stream().anyMatch(p -> p.getField("image").getValue().equals(imageName));
  }
}
