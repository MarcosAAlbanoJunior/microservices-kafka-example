package com.malbano.products.service.impl;

import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.dto.ProductCreatedEvent;
import com.malbano.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceImpl(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductRequest request) {

        String productId = UUID.randomUUID().toString();

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        productCreatedEvent.setProductId(productId);
        productCreatedEvent.setTitle(request.getTitle());
        productCreatedEvent.setPrice(request.getPrice());
        productCreatedEvent.setQuantity(request.getQuantity());

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
                kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

        future.whenComplete((result, exception) -> {
            if(exception != null){
                LOGGER.error("Failed to send message: " + exception.getMessage());
            } else {
                LOGGER.info("Message sent successfully: " + result.getRecordMetadata());
            }
        });

        LOGGER.info("Returning product id");
        return productId;
    }
}
