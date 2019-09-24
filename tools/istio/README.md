## Deployment instructions

`kubectl apply -f namespace.yaml`

`helm template istio-init --name istio-init --namespace istio-system | kubectl apply -f -`

`helm template ./istio --name istio --namespace istio-system --values values.yaml | kubectl apply -f -`
