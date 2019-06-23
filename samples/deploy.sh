kubectl apply -f ./prerequisites/namespaces.yaml
kubectl apply -f ./prerequisites/custom-metrics-api
kubectl apply -f ./prerequisites/kube-state-metrics
kubectl apply -f ./prerequisites/prometheus
kubectl apply -f ./prerequisites/k8s-prometheus-adapter
kubectl apply -f ./prerequisites/kube-state-metrics
kubectl apply -f ./prerequisites/metrics-server

kubectl apply -f ./kube-znn
kubectl apply -f ./kube-znn/kubow

kubectl get service/prometheus --namespace=monitoring -o json | jq .status.loadBalancer.ingress

kubectl get service/kube-znn -o json | jq .status.loadBalancer.ingress