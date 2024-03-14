package com.github.smsilva.wasp.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.messaging.PollableChannel;

import static org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter.ListenerMode.record;

@SpringBootApplication
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Bean
	public PollableChannel kafkaInput() {
		return new QueueChannel();
	}

	@Bean
	public KafkaMessageDrivenChannelAdapter<String, String> kafkaMessageDrivenChannelAdapter(
			KafkaMessageListenerContainer<String, String> container) {
		KafkaMessageDrivenChannelAdapter<String, String> adapter =
				new KafkaMessageDrivenChannelAdapter<>(container, record);
		adapter.setOutputChannel(kafkaInput());
		return adapter;
	}

	@Bean
	public KafkaMessageListenerContainer<String, String> kafkaMessageListenerContainer(
			ConsumerFactory<String, String> consumerFactory,
			@Value("${spring.kafka.consumer.group-id}") String groupId) throws Exception {
		ContainerProperties properties = new ContainerProperties("quickstart-events");
		properties.setAckMode(ContainerProperties.AckMode.RECORD);
		properties.setGroupId(groupId);
		return new KafkaMessageListenerContainer<>(consumerFactory, properties);
	}

}
