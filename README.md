# Spring Integration with Kafka example

## Native Image build

```bash
mvn -Pnative native:compile
```

![Kafka Producers and Consumers](/images/spring-integration-kafka.png)

## Download and Setup Apache Kafka scripts

https://kafka.apache.org/downloads

```bash
wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz
tar -xvzf kafka_2.13-3.7.0.tgz
cd kafka_2.13-3.7.0/bin
export KAFKA_BIN_PATH=$(pwd)

# Configure this on your .bashrc or .zshrc with the real KAFKA_BIN_PATH value
export PATH=${PATH}:${KAFKA_BIN_PATH}
```

## Apache Kafka Quickstart

https://kafka.apache.org/quickstart

### Start a Single Node Kafka Cluster using Docker

```bash
docker run \
  --rm \
  --detach \
  --name kafka \
  --hostname kafka \
  --network bridge \
  --publish 9092:9092 \
  apache/kafka:3.7.0
```

### Create Topics

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic "events-inbound" \
  --partitions 3

kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic "events-outbound" \
  --partitions 3
```

### Describe Topics

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic "events-inbound,events-outbound"
```

### Console Producer

```bash
kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-outbound" \
  --batch-size 1
```

### Console Consumer

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-outbound" \
  --group "console" \
  --from-beginning
```

### Spring Boot Kafka Producer and Consumer

#### Build

```bash
mvn package && docker build -t wasp-kafka-consumer .
```

#### Consumer 1

```bash
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8081 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_PRODUCER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
```

#### Consumer 2

```bash
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8082 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_PRODUCER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
```

#### Producing Messages

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

### Describe Consumer Group

```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group "events" \
  --offsets
```

## Outbound Events

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-outbound" \
  --from-beginning
```

```bash
mvn spring-boot:run
```

```bash
curl \
  --request POST \
  --url localhost:8080/events/send
  
http POST :8080/events/send
```

```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --execute \
  --reset-offsets \
  --topic "events-inbound" \
  --group "consumers" \
  --to-latest
  
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --execute \
  --reset-offsets \
  --to-offset 6 \
  --topic "events-inbound" \
  --group "consumers"
```

# Confluent

## Setup

```bash
export CONFLUENT_API_KEY=""
export CONFLUENT_API_SECRET=""
export CONFLUENT_BOOTSTRAP_SERVER=""
export CONFLUENT_ENVIRONMENT=""
export CONFLUENT_CLUSTER_NAME=""
export CONFLUENT_BASIC_TOKEN="Basic $(echo -n ${CONFLUENT_API_KEY?}:${CONFLUENT_API_SECRET?} | base64 | tr -d "\n")"
```

## Create a Topic

Reference: https://docs.confluent.io/platform/current/kafka-rest/api.html#topic-v3

```bash
curl \
  --request POST \
  --header "Content-Type: application/json" \
  --header "Authorization: ${CONFLUENT_BASIC_TOKEN?}" \
  --url https://${CONFLUENT_SERVER?}:443/kafka/v3/clusters/${CONFLUENT_CLUSTER_NAME?}/topics \
  --data '{"topic_name":"events-inbound","partitions_count":3}'

curl \
  --request POST \
  --header "Content-Type: application/json" \
  --header "Authorization: ${CONFLUENT_BASIC_TOKEN?}" \
  --url https://${CONFLUENT_SERVER?}:443/kafka/v3/clusters/${CONFLUENT_CLUSTER_NAME?}/topics \
  --data '{"topic_name":"events-outbound","partitions_count":3}'
```

## File Connector

### Create a Topic

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic "events-from-file" \
  --partitions 1
```

### Start a Consumer

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-from-file" \
  --group "console" \
  --from-beginning
```

### Create Event source file

```bash
mkdir -p /tmp/kafka/input

cat <<EOF > /tmp/kafka/input/events.source
{"id":"100100","name":"Simple name for ingestion #100100"}
EOF

cat /tmp/kafka/input/events.source
```

### Start the File Connector

```bash
connect-standalone.sh src/main/resources/kafka/connector/connect-standalone.properties src/main/resources/kafka/connector/connect-file-source.properties
```

### Produce events

```bash
for SEQUENCE in {100101..100110}; do
  export EVENT_ID=${SEQUENCE?}
  export EVENT_NAME="Simple name for ingestion #${EVENT_ID?}"
  envsubst < ./src/main/resources/kafka/input/event.json \
    | tee -a /tmp/kafka/input/events.source
  sleep 0
done
```

### Cleanup

#### Reset Connector offset

```bash
# Check offset.storage.file.filename property on src/main/resources/kafka/connector/connect-standalone.properties
rm /tmp/kafka/connect.offsets
```

#### Delete the Topic

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --delete \
  --topic "events-from-file"
```
