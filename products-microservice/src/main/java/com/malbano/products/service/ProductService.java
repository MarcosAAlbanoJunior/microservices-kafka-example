package com.malbano.products.service;

import com.malbano.products.dto.CreateProductRequest;

public interface ProductService {
    String createProduct(CreateProductRequest request);
}
