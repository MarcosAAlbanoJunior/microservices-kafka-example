package com.malbano.products.controller;

import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

    private final ProductService syncProductService;
    private final ProductService asyncProductService;

    public ProductController(@Qualifier("syncProductService") ProductService syncProductService,
                             @Qualifier("asyncProductService") ProductService asyncProductService) {
        this.syncProductService = syncProductService;
        this.asyncProductService = asyncProductService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Object> createProductSync(@RequestBody CreateProductRequest request){

        try {
            String productId = syncProductService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productId);
        } catch (Exception e) {
            LOGGER.error("Error creating product synchronously: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create product synchronously", e);
        }
    }

    @PostMapping("/async")
    public ResponseEntity<Object> createProductAsync(@RequestBody CreateProductRequest request){
        try {
            String productId = asyncProductService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(productId);
        } catch (Exception e) {
            LOGGER.error("Error creating product asynchronously: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create product asynchronously", e);
        }
    }
}
