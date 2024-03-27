# Local Cluster Example

## Create Cluster

```bash
k3d cluster create \
  --agents 3
```

## Application container build

```bash
export IMAGE="wasp-kafka-demo:0.0.3"

mvn package && docker build -t ${IMAGE?} .

k3d image import ${IMAGE?}
```

## Watch for Resources

```bash
watch -n 3 'kubectl get deploy,pods \
  --namespace wasp \
  --output wide'
```

## Namespace creation

```bash
kubectl create namespace wasp

kubectl config set-context \
  --current \
  --namespace wasp
```

## Secret config

```bash
kubectl apply \
  --namespace wasp \
  --filename - <<EOF
---
apiVersion: v1
kind: Secret
metadata:
  name: confluent-cloud-credentials-sasl
type: Opaque
stringData:
  CONFLUENT_API_KEY:          "${CONFLUENT_API_KEY?}"
  CONFLUENT_API_SECRET:       "${CONFLUENT_API_SECRET?}"
  CONFLUENT_BOOTSTRAP_SERVER: "${CONFLUENT_BOOTSTRAP_SERVER?}"
EOF
```

## Install Keda

```bash
helm repo add kedacore https://kedacore.github.io/charts

helm repo update kedacore

helm search repo kedacore/keda

helm install keda kedacore/keda \
  --create-namespace \
  --namespace keda \
  --wait
```

## Watch for Resources

```bash
watch -n 3 'kubectl get deploy,ta,so,hpa,pods \
  --namespace wasp \
  --output wide'
```

## Producing messages

```bash
confluent kafka topic produce events-inbound

{"id":"1","name":"Simple name for ingestion #1"}
{"id":"2","name":"Simple name for ingestion #2"}
{"id":"3","name":"Simple name for ingestion #3"}
{"id":"4","name":"Simple name for ingestion #4"}
{"id":"5","name":"Simple name for ingestion #5"}
{"id":"6","name":"Simple name for ingestion #6"}
{"id":"7","name":"Simple name for ingestion #7"}
{"id":"8","name":"Simple name for ingestion #8"}
{"id":"9","name":"Simple name for ingestion #9"}
{"id":"10","name":"Simple name for ingestion #10"}
```

## Keda metrics

```bash
kubectl get --raw "/apis/external.metrics.k8s.io/v1beta1/namespaces/YOUR_NAMESPACE/YOUR_METRIC_NAME?labelSelector=scaledobject.keda.sh%2Fname%3D{SCALED_OBJECT_NAME}"

kubectl -n wasp get scaledobject kafka-scaledobject -o jsonpath={.status.externalMetricNames}

kubectl get --raw "/apis/external.metrics.k8s.io/v1beta1/namespaces/wasp/s0-kafka-events-inbound?labelSelector=scaledobject.keda.sh%2Fname%3Dkafka-scaledobject"
```

## Running a local Prometheus Instance

```bash
docker run \
  --name prometheus \
  --rm \
  --detach \
  --network host \
  --volume $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.51.0 \
    --config.file=/etc/prometheus/prometheus.yml
```

## Running a local Grafana Instance

```bash
docker run \
  --name grafana \
  --rm \
  --detach \
  --network host \
  grafana/grafana:10.3.5-ubuntu
```
