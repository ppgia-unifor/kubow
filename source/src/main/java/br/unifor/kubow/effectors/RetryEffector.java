package br.unifor.kubow.effectors;

import com.google.gson.JsonParser;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.util.ClientBuilder;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Map.of;

public class RetryEffector extends KubowEffector {

  protected RetryEffector(String refID, String name) {
    super(refID, name);
  }

  @Override
  protected Outcome internalExecute(List<String> args) {
    var namespace = args.get(0);
    var virtualService = args.get(1);
    var attempts = args.get(2);
    var perTryTimeout = args.get(3);

    try {
      ApiClient client = ClientBuilder.standard().build();
      var json =
          "{\"spec\":{\"hosts\":[\"httpbin\"],\"http\":[{\"retries\":{\"attempts\":"
              + attempts
              + ",\"perTryTimeout\":\""
              + perTryTimeout
              + "s\"},\"route\":[{\"destination\":{\"host\":\"httpbin\",\"subset\":\"v1\"}}]}]}}";
      var body = new JsonParser().parse(json).getAsJsonObject();
      var call =
          client.buildCall(
              "/apis/networking.istio.io/v1beta1/namespaces/"
                  + namespace
                  + "/virtualservices/"
                  + virtualService,
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
      return Outcome.SUCCESS;
    } catch (ApiException | IOException e) {
      return Outcome.FAILURE;
    }
  }
}
