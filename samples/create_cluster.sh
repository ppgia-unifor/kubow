export NAME=kubeznn.k8s.local
export KOPS_STATE_STORE=s3://kops-k8s-clusters

aws s3 rm --recursive $KOPS_STATE_STORE
kubectl config delete-cluster $NAME
kubectl config delete-context $NAME

kops create cluster --zones us-east-1a --name $NAME --node-count=0

kops create ig monitoring --role node --name $NAME
kops create ig web --role node --name $NAME
kops create ig db --role node --name $NAME
kops create ig kubow --role node --name $NAME
kops delete ig nodes --name $NAME

kops update cluster --name $NAME --yes