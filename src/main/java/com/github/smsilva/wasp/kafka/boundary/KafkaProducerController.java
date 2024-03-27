package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class KafkaProducerController {

    private final KafkaProducerService kafkaProducerService;

    public KafkaProducerController(KafkaProducerService kafkaProducer) {
        this.kafkaProducerService = kafkaProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<Data> sendEvent() {
        Data data = new Data();

        kafkaProducerService.send(data);

        return ResponseEntity
                .ok()
                .body(data);
    }

}
