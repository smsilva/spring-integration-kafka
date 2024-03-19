package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.config.Channels;
import com.github.smsilva.wasp.kafka.entity.Data;
import com.github.smsilva.wasp.kafka.exceptions.BadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventsReceiver {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventsReceiver.class);

    @ServiceActivator(inputChannel = Channels.EVENTS_INPUT)
    public void handle(Message<Data> message, @Headers KafkaMessageHeaders headers) {
        Data payload = message.getPayload();
        log.info("Message: {}", message);
        log.info("Headers: {}", headers);
        log.info("Payload: {}", message.getPayload());
        log.info("  data.id: {}", message.getPayload().getId());
        log.info("  data.name: {}", message.getPayload().getName());

        if (payload instanceof BadData badData) {
            log.error("Bad data received: {}", badData);
            log.error("  topic: {}", badData.getFailedDeserializationInfo().getTopic());
            log.error("  exception.message: {}", badData.getFailedDeserializationInfo().getException().getMessage());
        }
    }

}
