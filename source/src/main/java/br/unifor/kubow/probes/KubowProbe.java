package br.unifor.kubow.probes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.sa.rainbow.translator.probes.AbstractRunnableProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * A probe that produces its data in json format
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public abstract class KubowProbe extends AbstractRunnableProbe {

  private static final Logger logger = LoggerFactory.getLogger(KubowProbe.class);
  private final ObjectWriter objectWriter;
  private final ObjectReader objectReader;

  public KubowProbe(String id, String type, long sleepTime, String[] args) {
    super(id, type, Kind.JAVA, sleepTime);
    var mapper = new ObjectMapper();
    objectWriter = mapper.writerFor(Map.class);
    objectReader = mapper.reader();
    logger.info("Probe {} initialized", id);
  }

  @Override
  public void run() {
    Thread currentThread = Thread.currentThread();
    while (thread() == currentThread && isActive()) {
      var data = collect();
      if (data != null) {
        logger.debug("Probe {} has collected value {}", id(), data);
        reportData(data);
      } else {
        logger.info("Probe {} has collected nothing", id());
      }
      sleep();
    }
  }

  protected ObjectWriter objectWriter() {
    return objectWriter;
  }

  protected ObjectReader objectReader() {
    return objectReader;
  }

  /**
   * Reports the data in json format.
   *
   * @param data
   */
  protected void reportData(Map<String, Object> data) {
    try {
      reportData(objectWriter.writeValueAsString(data));
    } catch (JsonProcessingException e) {
      logger.error("Probe {} can't send data. Reason: {}", this.name(), e.getMessage(), e);
    }
  }

  /**
   * Collects the data to be reported
   *
   * @return The data collected
   */
  protected abstract Map<String, Object> collect();

  private void sleep() {
    try {
      Thread.sleep(sleepTime());
    } catch (InterruptedException e) {
      // intentional ignore
    }
  }
}
