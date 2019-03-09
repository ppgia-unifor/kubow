package org.sa.rainbow.translator.probes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * A probe that produces its data in json format
 *
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public abstract class AbstractRunnableJsonProbe extends AbstractRunnableProbe {

  private static final Logger logger = getLogger(AbstractRunnableJsonProbe.class);
  private final ObjectWriter objectWriter;
  private final ObjectReader objectReader;

  public AbstractRunnableJsonProbe(String id, String type, long sleepTime, String[] args) {
    super(id, type, Kind.JAVA, sleepTime);
    var mapper = new ObjectMapper();
    objectWriter = mapper.writerFor(Map.class);
    objectReader = mapper.reader();
  }

  @Override
  public void run() {
    Thread currentThread = Thread.currentThread();
    while (thread() == currentThread && isActive()) {
      var data = collect();
      if (data != null) {
        logger.info("Probe {} has collected value {}", id(), data);
        reportData(data);
      } else {
        logger.info("probe {} has collected nothing", id());
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
