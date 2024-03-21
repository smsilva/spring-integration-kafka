# spring-integration-kafka

## Download and Setup Apache Kafka scripts

https://kafka.apache.org/downloads

```bash
wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz
tar -xvzf kafka_2.13-3.7.0.tgz
cd kafka_2.13-3.7.0/bin
export KAFKA_BIN_PATH=$(pwd)

# Configure this on your .bashrc or .zshrc with the real KAFKA_BIN_PATH value
export PATH={$PATH}:${KAFKA_BIN_PATH}
```

## Apache Kafka Quickstart

https://kafka.apache.org/quickstart

## Commands

```bash
docker run \
  --rm \
  --detach \
  --name kafka \
  --hostname kafka \
  --network bridge \
  --publish 9092:9092 apache/kafka:3.7.0
```

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic "events-inbound" \
  --partitions 2

kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --create \
  --topic "events-outbound" \
  --partitions 2
```

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic "events-inbound,events-outbound"
```

```bash
kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-inbound" \
  --batch-size 1
```

```bash
# Payload samples: send one by one
{"id":"1","name":"Simple name for ingestion #1"}
{"id":"2","name":"Simple name for ingestion #2"}
{"id":"3","name":"Simple name for ingestion #3"}
```

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "events-inbound" \
  --group "events" \
  --from-beginning
```

```bash
mvn package && docker build -t wasp-kafka-consumer .

docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8081 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-1" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
  
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8082 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="events-inbound" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-2" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
```

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
```

```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --execute \
  --reset-offsets \
  --topic "events-outbound" \
  --group "events" \
  --to-latest
```

# Confluent

## Setup

```bash
export CONFLUENT_API_KEY=""
export CONFLUENT_API_SECRET=""
export CONFLUENT_BOOTSTRAP_SERVER=""
export CONFLUENT_BASIC_TOKEN="Basic $(echo -n ${CONFLUENT_API_KEY?}:${CONFLUENT_API_SECRET?} | base64 | tr -d "\n")"
```

## Create a Topic

```bash
curl \
  --request POST \
  --header "Content-Type: application/json" \
  --header "Authorization: ${CONFLUENT_BASIC_TOKEN?}" \
  --url https://${CONFLUENT_SERVER?}:443/kafka/v3/clusters/lkc-pw9x7m/topics \
  --data '{"topic_name":"events-inbound"}'
```
