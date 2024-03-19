package com.github.smsilva.wasp.kafka.exceptions;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.springframework.kafka.support.serializer.FailedDeserializationInfo;

import java.util.function.Function;

public class FailedDataProvider implements Function<FailedDeserializationInfo, Data> {

    @Override
    public Data apply(FailedDeserializationInfo info) {
        return new BadData(info);
    }

}
