package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final MessageHandler kafkaProducerHandler;

    public KafkaProducerService(MessageHandler kafkaProducerHandler) {
        this.kafkaProducerHandler = kafkaProducerHandler;
    }

    public void send(Data data) throws MessagingException {
        log.info("event.id: {} event.name: {}", data.getId(), data.getName());

        Message<Data> message = new GenericMessage<>(data);
        kafkaProducerHandler.handleMessage(message);
    }

}
