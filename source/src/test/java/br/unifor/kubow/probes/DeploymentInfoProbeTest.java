package br.unifor.kubow.probes;

import edu.emory.mathcs.backport.java.util.Collections;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentCondition;
import io.kubernetes.client.openapi.models.V1DeploymentStatus;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;

public class DeploymentInfoProbeTest {

  @Test
  public void should_extract_latest_status() {
    var now = DateTime.now();
    var c2  = new V1DeploymentCondition();
    c2.setLastUpdateTime(now.minusDays(1));
    var c3  = new V1DeploymentCondition();
    c3.setLastUpdateTime(now.minusSeconds(1));
    var expected  = new V1DeploymentCondition();
    expected.setLastUpdateTime(now);

    var conditions = asList(expected, c2, c3);
    Collections.shuffle(conditions);

    var status = new V1DeploymentStatus();
    status.setConditions(conditions);
    var d = new V1Deployment();
    d.setStatus(status);

    var actual = DeploymentInfoProbe.extractLatestStatus(d);
    Assert.assertEquals(expected.getLastUpdateTime(), actual.getLastUpdateTime());
  }
}
