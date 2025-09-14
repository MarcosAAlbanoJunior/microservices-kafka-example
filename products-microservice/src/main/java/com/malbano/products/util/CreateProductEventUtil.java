package com.malbano.products.util;

import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.dto.ProductCreatedEvent;

public class CreateProductEventUtil {
    public static ProductCreatedEvent createProductEvent(String productId, CreateProductRequest request) {
        ProductCreatedEvent event = new ProductCreatedEvent();
        event.setProductId(productId);
        event.setTitle(request.getTitle());
        event.setPrice(request.getPrice());
        event.setQuantity(request.getQuantity());
        return event;
    }
}
