# Deploying kube-rainbow and znn exemplar

Deploy the Metrics Server in the `kube-system` namespace:

```bash
kubectl create -f ./metrics-server
```

After one minute the `metric-server` starts reporting CPU and memory usage for nodes and pods.

View nodes metrics:

```bash
kubectl get --raw "/apis/metrics.k8s.io/v1beta1/nodes" | jq .
```

View pods metrics:

```bash
kubectl get --raw "/apis/metrics.k8s.io/v1beta1/pods" | jq .
```

### Setting up a Custom Metrics Server

In order to scale based on custom metrics you need to have two components.
One component that collects metrics from your applications and stores them the [Prometheus](https://prometheus.io) time series database.
And a second component that extends the Kubernetes custom metrics API with the metrics supplied by the collect, the [k8s-prometheus-adapter](https://github.com/DirectXMan12/k8s-prometheus-adapter).

![Custom-Metrics-Server](https://github.com/stefanprodan/k8s-prom-hpa/blob/master/diagrams/k8s-hpa-prom.png)

You will deploy Prometheus and the adapter in a dedicated namespace. 

Create the `monitoring` namespace:

```bash
kubectl create -f ./namespaces.yaml
```

Deploy Prometheus v2 in the `monitoring` namespace:

*If you are deploying to GKE you might get an error saying: `Error from server (Forbidden): error when creating`
This will help you resolve that issue:* [RBAC on GKE](https://github.com/coreos/prometheus-operator/blob/master/Documentation/troubleshooting.md)

```bash
kubectl create -f ./prometheus
```

Generate the TLS certificates needed by the Prometheus adapter:

```bash
make certs
```

Deploy the Prometheus custom metrics API adapter:

```bash
kubectl create -f ./custom-metrics-api
```

List the custom metrics provided by Prometheus:

```bash
kubectl get --raw "/apis/custom.metrics.k8s.io/v1beta1" | jq .
```

Get the FS usage for all the pods in the `monitoring` namespace:

```bash
kubectl get --raw "/apis/custom.metrics.k8s.io/v1beta1/namespaces/monitoring/pods/*/fs_usage_bytes" | jq .
kubectl get --raw "/apis/custom.metrics.k8s.io/v1beta1/namespaces/default/pods/*/caddy_http_request_duration_seconds_bucket" | jq .

```

Create `znn` NodePort service and deployment in the `default` namespace:

Create password for mysql database

```bash
kubectl create secret generic mysql-pass --from-literal=password=znn_data
```

Create `znn` stack

```bash
kubectl apply -f ./znn
```

The `znn` app exposes a custom metric named `http_requests_total`.
The Prometheus adapter removes the `_total` suffix and marks the metric as a counter metric.

Get the total requests per second from the custom metrics API:

```bash
kubectl get --raw "/apis/custom.metrics.k8s.io/v1beta1/namespaces/default/pods/*/http_requests" | jq .
```

```json
{
  "kind": "MetricValueList",
  "apiVersion": "custom.metrics.k8s.io/v1beta1",
  "metadata": {
    "selfLink": "/apis/custom.metrics.k8s.io/v1beta1/namespaces/default/pods/%2A/http_requests"
  },
  "items": [
    {
      "describedObject": {
        "kind": "Pod",
        "namespace": "default",
        "name": "podinfo-6b86c8ccc9-kv5g9",
        "apiVersion": "/__internal"
      },
      "metricName": "http_requests",
      "timestamp": "2018-01-10T16:49:07Z",
      "value": "901m"
    },
    {
      "describedObject": {
        "kind": "Pod",
        "namespace": "default",
        "name": "podinfo-6b86c8ccc9-nm7bl",
        "apiVersion": "/__internal"
      },
      "metricName": "http_requests",
      "timestamp": "2018-01-10T16:49:07Z",
      "value": "898m"
    }
  ]
}
```

The `m` represents `milli-units`, so for example, `901m` means 901 milli-requests.
