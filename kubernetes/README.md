# Local Cluster Example

## Create Cluster

```bash
k3d cluster create \
  --agents 3
```

## Application container build

```bash
export IMAGE="wasp-kafka-demo:0.0.1"

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
  SPRING_KAFKA_USERNAME: "${SPRING_KAFKA_USERNAME?}"
  SPRING_KAFKA_PASSWORD: "${SPRING_KAFKA_PASSWORD?}"
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
