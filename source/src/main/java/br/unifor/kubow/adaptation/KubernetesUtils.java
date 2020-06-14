package br.unifor.kubow.adaptation;

import org.acmestudio.acme.PropertyHelper;
import org.acmestudio.acme.element.IAcmeComponent;
import org.acmestudio.basicmodel.core.AcmeRecordValue;
import org.acmestudio.basicmodel.core.AcmeSequenceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public abstract class KubernetesUtils {

  private static final Logger logger = LoggerFactory.getLogger(KubernetesUtils.class);

  public static String getContainerImage(IAcmeComponent deployment, String containerName) {
    var containers = (AcmeSequenceValue) deployment.getProperty("containers").getValue();
    if (containers != null) {
      var values = (List<AcmeRecordValue>) containers.getValues();

      for (var item : values) {
        if (PropertyHelper.toJavaVal(item.getField("name").getValue()).equals(containerName)) {
          return PropertyHelper.toJavaVal(item.getField("image").getValue()).toString();
        }
      }

    } else {
      logger.warn("Property containers of {} is null", deployment.getName());
    }
    return "";
  }
}
