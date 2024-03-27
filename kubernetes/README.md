# Local Cluster Example

## Create Cluster

```bash
k3d cluster create \
  --api-port 6550 \
  --port "8081:80@loadbalancer" \
  --agents 3
```

## Deploying an Apache Kafka Instance into k3d cluster

Environment variables based on this: [docker-compose.yml](https://github.com/apache/kafka/blob/trunk/docker/examples/jvm/single-node/plaintext/docker-compose.yml)

```bash
kubectl apply -f ./kubernetes/deploy/kafka/namespace.yaml
kubectl apply -f ./kubernetes/deploy/kafka/
```

```bash
kubectl wait pods \
  --namespace kafka \
  --selector app=kakfa \
  --for condition=Ready \
  --timeout=360s && \
kubectl logs \
  --namespace kafka \
  --selector app=kakfa \
  --follow
```

```bash
kubectl run \
  -it \
  --image apache/kafka:3.7.0 \
  --restart=Never \
  --rm kafka-client \
  --namespace default \
  --command -- bash
```

```bash
export PATH=$PATH:/opt/kafka/bin/

kafka-topics.sh \
  --bootstrap-server kafka.kafka.svc:9094 \
  --create \
  --topic "events-inbound" \
  --partitions 3
  
kafka-topics.sh \
  --bootstrap-server kafka.kafka.svc:9094 \
  --create \
  --topic "events-outbound" \
  --partitions 3
```

## Application container build

```bash
export IMAGE="wasp-kafka-demo:0.0.1"

mvn package && docker build -t ${IMAGE?} .

k3d image import ${IMAGE?}
```

## Watch for Resources

```bash
watch -n 3 'kubectl get cm,secret,deploy,pods,svc,ing \
  --namespace wasp \
  --output wide'
```

## Namespace creation

```bash
kubectl create namespace wasp
```

## Context config

```bash
kubectl config set-context \
  --current \
  --namespace wasp
```

```bash 
kubectl config get-contexts
```

## Application Deployment

```bash
kubectl apply \
  --namespace wasp \
  --filename ./kubernetes/deploy/ \
  --dry-run=server
```

```bash
kubectl apply \
  --namespace wasp \
  --filename ./kubernetes/deploy/ 
```

```bash
kubectl scale deployment wasp-kafka-demo --replicas 1
```

```bash
kubectl wait pods \
  --namespace wasp \
  --selector app=wasp-kafka-demo \
  --for condition=Ready \
  --timeout=360s && \
kubectl logs \
  --namespace wasp \
  --selector app=wasp-kafka-demo \
  --follow 
```

### Test

```bash
curl --include localhost:8081/actuator/health
```

```bash
for SEQUENCE in {1..20}; do
  curl \
    --silent \
    --request POST \
    --url localhost:8081/events/send
  
  sleep 0.5
  
  echo " Message #${SEQUENCE} was sent"
done
```

## Confluent Cloud config

To use Confluent, you must update deployment.yaml file accordingly:

- Update `configmap.yaml` `SPRING_PROFILES_ACTIVE` environment variable from `default` to `default,confluent`
- Update `deployment.yaml` file changing the secret from `in-cluster-kafka-credentials` to `confluent-cloud-credentials-sasl`

### Create Secret

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

## Deploy an in-cluster Scaled Object

```bash
kubectl apply \
  --filename ./kubernetes/deploy/in-cluster/keda-scaled-object.yaml
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

kubectl -n wasp get scaledobject wasp-kafka-demo-in-cluster -o jsonpath={.status.externalMetricNames}

kubectl get --raw "/apis/external.metrics.k8s.io/v1beta1/namespaces/wasp/s0-kafka-events-inbound?labelSelector=scaledobject.keda.sh%2Fname%3Dwasp-kafka-demo-in-cluster"
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
