package com.malbano.products.util;

import com.malbano.products.dto.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class LogSuccessfulSendUtil {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    public void logSuccessfulSend(SendResult<String, ProductCreatedEvent> result, boolean isSync) {
        String mode = isSync ? "synchronously" : "asynchronously";
        LOGGER.info("Message sent {} successfully: Topic={}, Partition={}, Offset={}",
                mode,
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset());
    }
}
