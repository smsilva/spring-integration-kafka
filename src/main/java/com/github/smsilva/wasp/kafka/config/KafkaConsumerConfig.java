package com.github.smsilva.wasp.kafka.config;

import com.github.smsilva.wasp.kafka.exceptions.FailedDeserializationData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.SubscribableChannel;

import java.util.Map;

import static org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.ListenerMode.record;

@Configuration
@ConditionalOnProperty(prefix = "spring.kafka", name = "enabled")
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties(null);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        properties.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        properties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        properties.put(ErrorHandlingDeserializer.VALUE_FUNCTION, FailedDeserializationData.class);

        properties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.github.smsilva.wasp.kafka.entity.Data");
        properties.put(JsonDeserializer.TRUSTED_PACKAGES, "com.github.smsilva");
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> kafkaMessageListenerContainer(
            ConsumerFactory<String, String> consumerFactory,
            KafkaProperties kafkaProperties,
            @Value("${spring.kafka.consumer.topic}") String topic) {
        ContainerProperties properties = new ContainerProperties(topic);
        properties.setAckMode(ContainerProperties.AckMode.RECORD);
        properties.setClientId(kafkaProperties.getConsumer().getClientId());
        properties.setGroupId(kafkaProperties.getConsumer().getGroupId());
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean(name = Channels.EVENTS_INPUT)
    public SubscribableChannel kafkaInput() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter(
            KafkaMessageListenerContainer<String, String> kafkaMessageListenerContainer) {
        KafkaMessageDrivenChannelAdapter<String, String> adapter =
                new KafkaMessageDrivenChannelAdapter<>(kafkaMessageListenerContainer, record);
        adapter.setOutputChannelName(Channels.EVENTS_INPUT);
        return adapter;
    }

}
