package com.malbano.products.service.impl;

import com.malbano.products.config.KafkaConfig;
import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.dto.ProductCreatedEvent;
import com.malbano.products.service.ProductService;
import com.malbano.products.util.CreateProductEventUtil;
import com.malbano.products.util.LogSuccessfulSendUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service("asyncProductService")
public class ProductServiceAsyncImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final LogSuccessfulSendUtil logUtil;

    public ProductServiceAsyncImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate, LogSuccessfulSendUtil logUtil) {
        this.kafkaTemplate = kafkaTemplate;
        this.logUtil = logUtil;
    }

    @Override
    public String createProduct(CreateProductRequest request) {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent event = CreateProductEventUtil.createProductEvent(productId, request);

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send(KafkaConfig.PRODUCT_CREATED_EVENTS_TOPIC, productId, event);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                LOGGER.error("Failed to send message asynchronously for product {}: {}",
                        productId, exception.getMessage(), exception);
            } else {
                logUtil.logSuccessfulSend(result, false);
            }
        });

        LOGGER.info("Returning product id asynchronously: {}", productId);
        return productId;
    }
}
