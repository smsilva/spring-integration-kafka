package com.github.smsilva.wasp.kafka.config;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.MessageHandler;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "enabled")
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Data> kafkaProducerFactory(KafkaProperties properties) {
        Map<String, Object> producerProperties = properties.buildProducerProperties(null);
        producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

        StringSerializer keySerializer = new StringSerializer();
        JsonSerializer<Data> valueSerializer = new JsonSerializer<>();

        return new DefaultKafkaProducerFactory<>(producerProperties, keySerializer, valueSerializer);
    }

    @Bean
    public KafkaTemplate<String, Data> kafkaTemplate(ProducerFactory<String, Data> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public MessageHandler kafkaProducerHandler(
            KafkaTemplate<String, Data> kafkaTemplate,
            @Value("${spring.kafka.producer.topic}") String topic,
            @Value("${spring.kafka.producer.message-key}") String messageKey) {
        KafkaProducerMessageHandler<String, Data> handler = new KafkaProducerMessageHandler<>(kafkaTemplate);
        handler.setTopicExpression(new LiteralExpression(topic));
        handler.setMessageKeyExpression(new LiteralExpression(messageKey));
        return handler;
    }

}
