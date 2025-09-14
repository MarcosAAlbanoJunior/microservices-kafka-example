package com.malbano.emailnotification.service.impl;

import com.malbano.emailnotification.dto.ProductCreatedEvent;
import com.malbano.emailnotification.service.ProductNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProductNotificationServiceImpl implements ProductNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductNotificationServiceImpl.class);

    public void processProductCreated(ProductCreatedEvent event) {
        LOGGER.info("Processing product notification for: {}", event.getTitle());
        try {
            LOGGER.info("mock email enviado");
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}