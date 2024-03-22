package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class KafkaProducerController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(KafkaProducerController.class);

    private final KafkaProducerService kafkaProducer;

    public KafkaProducerController(KafkaProducerService kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<Data> sendEvent() {
        Data data = new Data();

        log.info("event.id: {} event.name: {}", data.getId(), data.getName());

        kafkaProducer.send(data);

        return ResponseEntity
                .ok()
                .body(data);
    }

}
