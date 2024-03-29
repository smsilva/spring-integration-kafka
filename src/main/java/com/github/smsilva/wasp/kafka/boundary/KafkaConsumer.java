package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.config.Channels;
import com.github.smsilva.wasp.kafka.entity.Data;
import com.github.smsilva.wasp.kafka.exceptions.BadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.kafka.support.converter.KafkaMessageHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final int simulationSeconds;
    private final AtomicInteger counter = new AtomicInteger(0);

    public KafkaConsumer(@Value("${spring.kafka.simulation.seconds:0}") int simulationSeconds) {
        this.simulationSeconds = simulationSeconds;
    }

    @ServiceActivator(inputChannel = Channels.EVENTS_INPUT)
    public void handle(Message<Data> message, @Headers KafkaMessageHeaders headers) throws Exception {
        int invocations = counter.addAndGet(1);

        Data payload = message.getPayload();
        log.info("Message[{}]: {}", invocations,  message);
        log.info("Headers: {}", headers);
        log.info("Payload: {}", message.getPayload());
        log.info("  data.id: {}", message.getPayload().getId());
        log.info("  data.name: {}", message.getPayload().getName());

        if (payload instanceof BadData badData) {
            log.error("Bad data received: {}", badData);
            log.error("  topic: {}", badData.getFailedDeserializationInfo().getTopic());
            log.error("  exception.message: {}", badData.getFailedDeserializationInfo().getException().getMessage());
        }

        simulation();
    }

    private void simulation() throws InterruptedException {
        if (this.simulationSeconds > 0) {
            log.info("Simulating slow processing waiting for {} seconds", this.simulationSeconds);
            TimeUnit.SECONDS.sleep(simulationSeconds);
            log.info("Processing done.");
        }
    }

}
