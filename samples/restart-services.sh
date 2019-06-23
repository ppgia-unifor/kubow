kubectl delete -f ./prerequisites/prometheus/prometheus-dep.yaml
kubectl apply -f ./prerequisites/prometheus/prometheus-dep.yaml

kubectl scale --replicas=1 deployment/kube-znn
sleep 1
kubectl set image deployment.v1.apps/kube-znn znn=cmendes/znn:high --record

kubectl delete -f ./kube-znn/kubow
kubectl delete -f ./kube-znn/kubow-scaling
kubectl delete -f ./kube-znn/kubow-tunning

kubectl apply -f ./kube-znn/kubow-$1
kubectl apply -f ./kube-znn/kubow