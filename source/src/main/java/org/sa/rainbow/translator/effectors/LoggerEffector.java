package org.sa.rainbow.translator.effectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.String.*;

/** @author Carlos Mendes (cmendesce@gmail.com) */
public class LoggerEffector extends AbstractEffector {

  private static final Logger logger = LoggerFactory.getLogger(LoggerEffector.class);

  public LoggerEffector(String refID, String name) {
    super(refID, name, Kind.JAVA);
  }

  @Override
  public Outcome execute(List<String> args) {
    var id = args.get(0);
    var name = args.get(1);

    if (args.size() > 2) {
      logger.info(
          "Running effector {}@{} with arguments [{}]",
          id,
          name,
          join(",", args.subList(2, args.size() - 1)));
    } else {
      logger.info("Running effector {}@{} with no arguments", id, name);
    }

    return Outcome.SUCCESS;
  }
}
