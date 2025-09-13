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

@Service("syncProductService")
public class ProductServiceSyncImpl implements ProductService {

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductServiceSyncImpl(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public String createProduct(CreateProductRequest request) throws Exception{

        String productId = UUID.randomUUID().toString();

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();
        productCreatedEvent.setProductId(productId);
        productCreatedEvent.setTitle(request.getTitle());
        productCreatedEvent.setPrice(request.getPrice());
        productCreatedEvent.setQuantity(request.getQuantity());

       //sync
       SendResult<String, ProductCreatedEvent> result =
                kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent).get();
        LOGGER.info("Message async sent successfully: {}", result.getRecordMetadata());
        LOGGER.info("Partition: {}", result.getRecordMetadata().partition());
        LOGGER.info("Topic: {}", result.getRecordMetadata().topic());
        LOGGER.info("Offset: {}", result.getRecordMetadata().offset());

        LOGGER.info("**** Returning product id sync ****");
        return productId;
    }
}
