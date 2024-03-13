package com.github.smsilva.wasp.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

@SpringBootApplication
public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@KafkaListener(id = "${spring.kafka.consumer.group-id}", topics = "quickstart-events")
	public void listen(String in, @Header(KafkaHeaders.GROUP_ID) String groupId) {
		log.info("Received: {} Group ID: {}", in, groupId);
	}

}
