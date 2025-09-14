package com.malbano.emailnotification.service;

import com.malbano.emailnotification.dto.ProductCreatedEvent;

public interface ProductNotificationService {
    void processProductCreated(ProductCreatedEvent event);
}
