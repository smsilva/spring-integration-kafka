# spring-integration-kafka

## Apache Kafka Quickstart

https://kafka.apache.org/quickstart

## Downloads

https://kafka.apache.org/downloads

```bash
wget https://downloads.apache.org/kafka/3.7.0/kafka_2.13-3.7.0.tgz
tar -xvzf kafka_2.13-3.7.0.tgz
cd kafka_2.13-3.7.0/bin
export KAFKA_BIN_PATH=$(pwd)

# Configure this on your .bashrc or .zshrc with the real KAFKA_BIN_PATH value
export PATH={$PATH}:${KAFKA_BIN_PATH}
```

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
  --topic "quickstart-events" \
  --partitions 2
```

```bash
kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic "quickstart-events"
```

```bash
kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic "quickstart-events" \
  --batch-size 1
```

```bash
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic "quickstart-events" \
  --from-beginning
```

```bash
mvn package && docker build -t wasp-kafka-consumer .

docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8081 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="quickstart-events" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-1" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
  
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8082 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="quickstart-events" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="events-consumer-2" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="events" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
  
docker run \
  --rm \
  --network host \
  --env SERVER_PORT=8083 \
  --env SPRING_KAFKA_CONSUMER_TOPIC="quickstart-events" \
  --env SPRING_KAFKA_CONSUMER_CLIENT_ID="orders-consumer-1" \
  --env SPRING_KAFKA_CONSUMER_GROUP_ID="orders" \
  --env SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  wasp-kafka-consumer:latest
```

```bash
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --describe \
  --group events \
  --group orders \
  --offsets
```
