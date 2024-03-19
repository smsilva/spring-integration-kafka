package com.github.smsilva.wasp.kafka.exceptions;

import com.github.smsilva.wasp.kafka.entity.Data;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

public class ErrorHandlingDeserializerData extends ErrorHandlingDeserializer<Data> {
}
