# Local Cluster Example

## Create a k3d cluster

```bash
k3d cluster create \
  --api-port 6550 \
  --port "8081:80@loadbalancer" \
  --agents 3
```

## Single Node Apache Kafka Instance deployment

### Deploy

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

### Setup

Split the terminal to make room for consumers first and client after.

### Client

```bash
kubectl run \
  -it \
  --image apache/kafka:3.7.0 \
  --restart=Never \
  --rm kafka-client \
  --namespace default \
  --command -- bash
```

### Create Topics

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

### Describe Topics

```bash
kafka-topics.sh \
  --bootstrap-server kafka.kafka.svc:9094 \
  --describe \
  --topic "events-inbound,events-outbound"
```

### Consumer 1 (POD)

```bash
kubectl run \
  -it \
  --image apache/kafka:3.7.0 \
  --restart=Never \
  --rm "kafka-consumer-1" \
  --namespace default \
  --command -- bash
```

### Consumer 2 (POD)

```bash
kubectl run \
  -it \
  --image apache/kafka:3.7.0 \
  --restart=Never \
  --rm "kafka-consumer-2" \
  --namespace default \
  --command -- bash
```

### Start consuming

```bash
export PATH=$PATH:/opt/kafka/bin/

kafka-console-consumer.sh \
  --bootstrap-server kafka.kafka.svc:9094 \
  --topic "events-inbound" \
  --group "console" \
  --from-beginning
```

### Producer

```bash
kafka-console-producer.sh \
  --bootstrap-server kafka.kafka.svc:9094 \
  --topic "events-inbound" \
  --batch-size 1
```

## Application Deployment

### Application container build

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

### Dry-run

```bash
kubectl apply \
  --namespace wasp \
  --filename ./kubernetes/deploy/application/ \
  --dry-run=server
```

### Apply resources

```bash
kubectl apply \
  --namespace wasp \
  --filename ./kubernetes/deploy/application/
```

### Scale up the deployment to 2 replicas

```bash
kubectl scale deployment wasp-kafka-demo --replicas 2
```

### Wait and logs follow

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

```bash
kubectl logs --follow <CONSUMER_1_POD>
```

### Test

```bash
curl --include localhost:8081/actuator/health
```

### Produce messages

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

### Produce messages

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

## Keda metrics

```bash
kubectl get scaledobject wasp-kafka-demo-in-cluster \
  --namespace wasp \
  --output jsonpath={.status.externalMetricNames}
```

```bash
kubectl get --raw "/apis/external.metrics.k8s.io/v1beta1/namespaces/wasp/s0-kafka-events-inbound?labelSelector=scaledobject.keda.sh%2Fname%3Dwasp-kafka-demo-in-cluster"
```
