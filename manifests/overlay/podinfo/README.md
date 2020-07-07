# podinfo

Podinfo is a tiny web application made with Go that showcases best practices of running microservices in Kubernetes. The source code is available [here](https://github.com/stefanprodan/podinfo).

## Run the app in Kubernetes

Apply the deployment and service.

```sh
kubectl apply -f https://raw.githubusercontent.com/stefanprodan/podinfo/master/kustomize/deployment.yaml
kubectl apply -f https://raw.githubusercontent.com/stefanprodan/podinfo/master/kustomize/service.yaml
```

Run `kubectl get pod` to see the created pods. The result will looks like bellow

```sh
NAME                       READY   STATUS    RESTARTS   AGE
podinfo-68dfd5768b-5zfdj   1/1     Running   0          2m5s
```

Run `kubectl get svc` to see the created services. The result will looks like bellow

```sh
NAME         TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
podinfo      ClusterIP   10.104.170.217   <none>        9898/TCP,9999/TCP   98s
```

Run `kubectl port-forward svc/podinfo 9898:9898` to check the app on browser through the port [9898](http://localhost:9898/).
