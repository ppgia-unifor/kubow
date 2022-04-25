package br.unifor.kubow.effectors;

import br.unifor.kubow.services.kubernetes.KubernetesClientFactory;
import com.google.gson.JsonParser;
import io.kubernetes.client.openapi.ApiException;

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
    var host = args.get(2);
    var subset = args.get(3);
    var attempts = args.get(4);
    var perTryTimeout = args.get(5);

    try {
      var client = KubernetesClientFactory.defaultClient();
      var json =
              "{\"spec\":{\"hosts\":[\"" +
                      host +
                      "\"],\"http\":[{\"retries\":{\"attempts\":" +
                      attempts +
                      ",\"perTryTimeout\":\"" +
                      perTryTimeout +
                      "\"},\"route\":[{\"destination\":{\"host\":\"" +
                      host +
                      "\",\"subset\":\"" +
                      subset +
                      "\"}}]}]}}";

      var body = new JsonParser().parse(json).getAsJsonObject();
      var patchRequest =
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
              new String[] {"BearerToken"},
              null);

      var response = client.execute(patchRequest);
      if (response.getStatusCode() == 200) {
        return Outcome.SUCCESS;
      } else {
        return Outcome.FAILURE;
      }
    } catch (ApiException e) {
      // TODO logging
      return Outcome.FAILURE;
    }
  }
}
