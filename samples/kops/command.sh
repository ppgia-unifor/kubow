kops create cluster \
       --state "s3://dissertation-tests" \
       --zones "us-east-1d,us-east-1f"  \
       --master-count 1 \
       --master-size=m3.medium \
       --node-count 3 \
       --node-size=t2.small \
       --name cmendes.dissertation.k8s.local \
       --yes

kops create ig monitoring --state "s3://dissertation-tests"
kops update cluster cmendes.dissertation.k8s.local --state "s3://dissertation-tests" --yes

kops create ig testing --state "s3://dissertation-tests"
kops delete ig testing --state "s3://dissertation-tests"
kops update cluster cmendes.dissertation.k8s.local --state "s3://dissertation-tests" --yes

kops create ig db --state "s3://dissertation-tests"
kops update cluster cmendes.dissertation.k8s.local --state "s3://dissertation-tests" --yes

kops rolling-update cluster cmendes.dissertation.k8s.local --state "s3://dissertation-tests" --yes

kops delete cluster --state "s3://dissertation-tests"  --name cmendes.dissertation.k8s.local --yes

kops update cluster --state "s3://dissertation-tests"  --name cmendes.dissertation.k8s.local --yes


http://ae27c2f134a4011e98a961268ffbf0e2-645066884.us-east-1.elb.amazonaws.com/news.php

rate(caddy_http_response_latency_seconds_count{namespace!="",pod=~"kube-znn.*"}[1m])