package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Data data = new Data();

        log.info("event.id: {} event.name: {}", data.getId(), data.getName());

        Message<Data> message = new GenericMessage<>(data);
        kafkaProducerHandler.handleMessage(message);

        return ResponseEntity
                .ok()
                .body(data);
    }

}
