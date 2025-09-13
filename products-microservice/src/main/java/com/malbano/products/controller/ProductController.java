package com.malbano.products.controller;

import com.malbano.products.dto.CreateProductRequest;
import com.malbano.products.exceptions.ErrorMessage;
import com.malbano.products.service.ProductService;
import com.malbano.products.service.impl.ProductServiceAsyncImpl;
import com.malbano.products.service.impl.ProductServiceSyncImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productServiceSync;
    private final ProductService productServiceAsync;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductController(@Qualifier("syncProductService") ProductService productServiceSync,
                             @Qualifier("asyncProductService") ProductService productServiceAsync) {
        this.productServiceSync = productServiceSync;
        this.productServiceAsync = productServiceAsync;
    }

    @PostMapping("/sync")
    public ResponseEntity<Object> createProductSync(@RequestBody CreateProductRequest request){

        String productId;
        try {
            productId = productServiceSync.createProduct(request);
        }
        catch (Exception e){
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(new Date(), e.getMessage(), "/products"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

    @PostMapping("/async")
    public ResponseEntity<Object> createProductAsync(@RequestBody CreateProductRequest request){

        String productId;
        try {
            productId = productServiceAsync.createProduct(request);
        }
        catch (Exception e){
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(new Date(), e.getMessage(), "/products"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }
}
