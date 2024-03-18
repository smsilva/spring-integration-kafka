package com.github.smsilva.wasp.kafka.boundary;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventSenderController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EventSenderController.class);

    private final MessageHandler kafkaProducerHandler;

    @Autowired
    public EventSenderController(MessageHandler kafkaProducerHandler) {
        this.kafkaProducerHandler = kafkaProducerHandler;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEvent() {
        log.info("Sending event");
        log.info("kafkaProducerHandler: {}", kafkaProducerHandler);
        kafkaProducerHandler.handleMessage(new GenericMessage<>("Hello, Kafka!"));
        return ResponseEntity.ok().build();
    }

}
