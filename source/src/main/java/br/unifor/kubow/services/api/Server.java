package br.unifor.kubow.services.api;

import br.unifor.kubow.KubowApplication;
import com.google.gson.Gson;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowMaster;
import spark.ResponseTransformer;

import java.util.HashMap;

import static org.sa.rainbow.core.Rainbow.instance;
import static spark.Spark.*;


/**
 * @author Carlos Mendes (cmendesce@gmail.com)
 */
public class Server {

  /**
   * * `GET /` prints runtime information
   * * `GET /version` prints kubow version and git commit hash
   * * `GET /metrics` return prometheus metrics
   * * `GET /healthz` used by Kubernetes liveness probe
   * * `GET /readyz` used by Kubernetes readiness probe
   * * `GET /properties` returns the environment variables as a JSON array
   * * `GET /configs` returns a JSON with all properties
   */

  private static final Gson gson = new Gson();

  public static RainbowMaster master() {
    return (RainbowMaster) Rainbow.instance().getRainbowMaster();
  }

  public static ResponseTransformer json() {
    return gson::toJson;
  }

  public static void init() {

    after((req, res) -> res.type("application/json"));

    get("/", (req, res) -> "Not implemented yet");
    get("/version", (req, res) -> "Not implemented yet");
    get("/healthz", (req, res) -> "Ok");

    get("/readyz", (req, res) -> {
      if (KubowApplication.isReady()) {
        return "Kubow is ready";
      } else {
        throw halt(500, "Kubow is not ready");
      }
    }, json());
    get("/probes", (req, res) -> master().probeDesc(), json());
    get("/gauges", (req, res) -> master().gaugeDesc(), json());
    get("/effectors", (req, res) -> master().effectorDesc(), json());
    get("/models", (req, res) -> master().modelsManager().getRegisteredModelTypes(), json());

    /**
     * returns all properties set in rainbow.properties
     */
    get("/properties", (req, res) -> instance().allProperties(), json());

    get("/configs", (req, res) -> {
      var configs = new HashMap<String, String>();
      configs.put("rainbow.config", System.getProperty("rainbow.config"));
      configs.put("rainbow.target", System.getProperty("rainbow.target"));
      return configs;
    }, gson::toJson);

  }
}
