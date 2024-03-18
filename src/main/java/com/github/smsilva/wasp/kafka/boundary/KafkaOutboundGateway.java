package com.github.smsilva.wasp.kafka.boundary;

import com.github.smsilva.wasp.kafka.config.Channels;
import org.springframework.integration.annotation.Gateway;

public interface KafkaOutboundGateway {

    @Gateway(requestChannel = Channels.EVENTS_OUTPUT)
    void send(String message);

}
