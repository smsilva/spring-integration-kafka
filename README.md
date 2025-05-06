# Spring Integration with Kafka example

## Native Image build

```bash
mvn -Pnative native:compile
```

![Kafka Producers and Consumers](/images/spring-integration-kafka.png)

## Download and Setup Apache Kafka scripts

https://kafka.apache.org/downloads

```bash
mkdir --parents ${HOME}/bin
wget --output-document /tmp/kafka_2.13-4.0.0.tgz https://downloads.apache.org/kafka/4.0.0/kafka_2.13-4.0.0.tgz
tar -xvzf /tmp/kafka_2.13-4.0.0.tgz --directory ${HOME}/bin

cat <<EOF >> ${HOME}/.bashrc

export KAFKA_BIN_PATH="${HOME}/bin/kafka_2.13-4.0.0/bin"

if ! grep --quiet "\${KAFKA_BIN_PATH}" <<< "\${PATH}"; then
  export PATH="\${KAFKA_BIN_PATH}:\${PATH}"
fi
EOF
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
  apache/kafka:4.0.0
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

### Azure Event Hubs Authentication + Java JDK 23

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Djava.security.manager=allow" 
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
# Azure Event Hubs

## Setup

```bash
export AZURE_EVENTHUBS_CONNECTION_STRING="Endpoint=sb://<EVENT_HUB_NAMESPACE_NAME>.servicebus.windows.net/;SharedAccessKeyName=manage;SharedAccessKey=<YOUR_SHARED_ACCESS_KEY_VALUE>;EntityPath=<EVENT_HUB_NAME>"
export AZURE_EVENTHUBS_BOOTSTRAP_SERVER="<EVENT_HUB_NAMESPACE_NAME>.servicebus.windows.net:9093"
export AZURE_EVENTHUBS_USERNAME='$ConnectionString'
export SPRING_PROFILES_ACTIVE='default,eventhubs'
export SPRING_KAFKA_CONSUMER_TOPIC='<EVENT_HUB_NAME>'
export KAFKA_OPTS="-Djava.security.manager=allow" # Before Java 24
```

### Optional

```bash
cat <<EOF > console.properties
security.protocol: SASL_SSL
sasl.mechanism: PLAIN
sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username='\$ConnectionString' password='${AZURE_EVENTHUBS_CONNECTION_STRING}';
session.timeout.ms: 60000
EOF
````

```bash
kafka-console-consumer.sh \
  --bootstrap-server ${AZURE_EVENTHUBS_BOOTSTRAP_SERVER} \
  --consumer.config console.properties \
  --topic "${SPRING_KAFKA_CONSUMER_TOPIC?}" \
  --group "console" \
  --from-beginning
```

```bash
kafka-console-producer.sh \
  --bootstrap-server ${AZURE_EVENTHUBS_BOOTSTRAP_SERVER} \
  --producer.config console.properties \
  --topic "${SPRING_KAFKA_PRODUCER_TOPIC?}" \
  --batch-size 1
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
