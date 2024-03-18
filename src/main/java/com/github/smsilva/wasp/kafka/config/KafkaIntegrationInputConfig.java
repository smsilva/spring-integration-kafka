package com.github.smsilva.wasp.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.messaging.SubscribableChannel;

import static org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.ListenerMode.record;

@Configuration
public class KafkaIntegrationInputConfig {

    @Bean(Channels.EVENTS_INPUT)
    public SubscribableChannel kafkaInput() {
        return new PublishSubscribeChannel();
    }

    @Bean
    public KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter(
            KafkaMessageListenerContainer<String, String> container) {
        KafkaMessageDrivenChannelAdapter<String, String> adapter =
                new KafkaMessageDrivenChannelAdapter<>(container, record);
        adapter.setOutputChannelName(Channels.EVENTS_INPUT);
        return adapter;
    }

}
