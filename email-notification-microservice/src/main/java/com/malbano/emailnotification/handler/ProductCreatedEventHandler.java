package com.malbano.emailnotification.handler;

import com.malbano.emailnotification.dto.ProductCreatedEvent;
import com.malbano.emailnotification.service.ProductNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics="product-created-events-topic", groupId = "product-created-events")
public class ProductCreatedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ProductCreatedEventHandler.class);

    private final ProductNotificationService notificationService;

    public ProductCreatedEventHandler(ProductNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaHandler
    public void handle(@Payload ProductCreatedEvent event) {
        log.info("Processing ProductCreatedEvent: productId={}, title='{}'",
                event.getProductId(), event.getTitle());

        try {
            notificationService.processProductCreated(event);
            log.info("Successfully processed ProductCreatedEvent: productId={}", event.getProductId());
        } catch (Exception e) {
            log.error("Error processing ProductCreatedEvent: productId={}, error={}",
                    event.getProductId(), e.getMessage(), e);
            throw e;
        }
    }
}
