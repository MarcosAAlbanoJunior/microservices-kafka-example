package com.malbano.products.service.impl;

import com.malbano.products.config.KafkaConfig;
import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.dto.ProductCreatedEvent;
import com.malbano.products.service.ProductService;
import com.malbano.products.util.CreateProductEventUtil;
import com.malbano.products.util.LogSuccessfulSendUtil;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service("syncProductService")
public class ProductServiceSyncImpl implements ProductService {

    private static final int TIMEOUT_SECONDS = 30;
    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final LogSuccessfulSendUtil logUtil;

    public ProductServiceSyncImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate, LogSuccessfulSendUtil logUtil) {
        this.kafkaTemplate = kafkaTemplate;
        this.logUtil = logUtil;
    }

    @Override
    public String createProduct(CreateProductRequest request) throws Exception {
        String productId = UUID.randomUUID().toString();
        ProductCreatedEvent event = CreateProductEventUtil.createProductEvent(productId, request);

        try {
            SendResult<String, ProductCreatedEvent> result = kafkaTemplate
                    .send(KafkaConfig.PRODUCT_CREATED_EVENTS_TOPIC, productId, event)
                    .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            logUtil.logSuccessfulSend(result, true);
            LOGGER.info("Returning product id synchronously: {}", productId);
            return productId;

        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            LOGGER.error("Failed to send message synchronously for product {}: {}", productId, e.getMessage());
            throw new Exception("Failed to publish product creation event", e);
        }
    }

}
