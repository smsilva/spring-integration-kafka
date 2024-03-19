package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/events")
public class EventSenderController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EventSenderController.class);

    private final MessageHandler kafkaProducerHandler;

    public EventSenderController(MessageHandler kafkaProducerHandler) {
        this.kafkaProducerHandler = kafkaProducerHandler;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendEvent() {
        log.info("Sending event");

        Data data = new Data();
        data.setId(UUID.randomUUID().toString());
        data.setName("Simple name");

        kafkaProducerHandler.handleMessage(new GenericMessage<Data>(data));
        return ResponseEntity.ok().build();
    }

}
