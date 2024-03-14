package com.github.smsilva.wasp.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class ServiceActivatorExample {

    private static final Logger log = LoggerFactory.getLogger(ServiceActivatorExample.class);

    @ServiceActivator(inputChannel = "kafkaInput")
    public void exampleHandler(GenericMessage<String> message) {
        log.info("Received: {}", message);
    }

}
