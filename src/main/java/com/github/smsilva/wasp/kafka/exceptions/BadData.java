package com.github.smsilva.wasp.kafka.exceptions;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

public class BadData extends Data {

    private final FailedDeserializationInfo failedDeserializationInfo;

    public BadData(FailedDeserializationInfo failedDeserializationInfo) {
        this.failedDeserializationInfo = failedDeserializationInfo;
    }

    public FailedDeserializationInfo getFailedDeserializationInfo() {
        return this.failedDeserializationInfo;
    }

}
