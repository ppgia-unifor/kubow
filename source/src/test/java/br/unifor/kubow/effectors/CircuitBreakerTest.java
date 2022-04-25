package br.unifor.kubow.effectors;

import br.unifor.kubow.services.kubernetes.KubernetesClientFactory;
import com.google.gson.JsonParser;
import io.kubernetes.client.openapi.ApiException;
import org.junit.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;

public class CircuitBreakerTest {

    @Test
    public void test() throws ApiException {
        var client = KubernetesClientFactory.defaultClient();

        var json =
              "{"
            + "  \"spec\": {"
            + "    \"host\": \"httpbin\","
            + "    \"trafficPolicy\": {"
            + "      \"connectionPool\": {"
            + "        \"tcp\": {"
            + "          \"maxConnections\": 1"
            + "        },"
            + "        \"http\": {"
            + "          \"http1MaxPendingRequests\": 2,"
            + "          \"maxRequestsPerConnection\": 2"
            + "        }"
            + "      },"
            + "      \"outlierDetection\": {"
            + "        \"consecutive5xxErrors\": 1,"
            + "        \"interval\": \"2s\","
            + "        \"baseEjectionTime\": \"3m\","
            + "        \"maxEjectionPercent\": 100"
            + "      }"
            + "    }"
            + "  }"
            + "}";

        var body = new JsonParser().parse(json).getAsJsonObject();
        var call =
                client.buildCall(
                        "/apis/networking.istio.io/v1beta1/namespaces/default/destinationrules/httpbin",
                        "PATCH",
                        emptyList(),
                        emptyList(),
                        body,
                        of("Content-Type", "application/merge-patch+json"),
                        emptyMap(),
                        emptyMap(),
                        new String[] {},
                        null);
        var response = client.execute(call);
        System.out.println(response.getData());
    }
}
