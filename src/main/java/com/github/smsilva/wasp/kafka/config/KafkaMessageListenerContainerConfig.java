package com.github.smsilva.wasp.kafka.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

@Configuration
public class KafkaMessageListenerContainerConfig {

    @Bean
    public KafkaMessageListenerContainer<String, String> kafkaMessageListenerContainer(
            ConsumerFactory<String, String> consumerFactory,
            @Value("${spring.kafka.consumer.topic}") String topic,
            @Value("${spring.kafka.consumer.client-id}") String clientId,
            @Value("${spring.kafka.consumer.group-id}") String groupId) {
        ContainerProperties properties = new ContainerProperties(topic);
        properties.setAckMode(ContainerProperties.AckMode.RECORD);
        properties.setClientId(clientId);
        properties.setGroupId(groupId);
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

}
