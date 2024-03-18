package com.github.smsilva.wasp.kafka.control;

import com.github.smsilva.wasp.kafka.config.Channels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventsReceiver {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventsReceiver.class);

    @ServiceActivator(inputChannel = Channels.EVENTS_INPUT)
    public void exampleHandler(Message<?> message) {
        log.info("Received: {}", message);
    }

}
