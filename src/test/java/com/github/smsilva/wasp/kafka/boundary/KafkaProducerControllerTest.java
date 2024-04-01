package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerControllerTest {

    @Mock
    KafkaProducerService kafkaProducerService;

    @Test
    void testSendEvent() {
        KafkaProducerController kafkaProducerController = new KafkaProducerController(kafkaProducerService);

        ResponseEntity<Data> response = kafkaProducerController.sendEvent();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(kafkaProducerService, times(1)).send(any());
    }

}
