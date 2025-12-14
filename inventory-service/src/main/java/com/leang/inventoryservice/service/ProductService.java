package com.leang.inventoryservice.service;

import com.leang.inventoryservice.model.dto.request.BatchProductRequest;
import com.leang.inventoryservice.model.dto.request.ProductRequest;
import com.leang.inventoryservice.model.dto.response.PagedResponse;
import com.leang.inventoryservice.model.entity.Product;

import java.util.List;

public interface ProductService {
    PagedResponse<Product> getAllProducts(Integer page, Integer size);

    Product getProductById(Long id);

    Product updateProductById(Long id, ProductRequest productRequest);

    Product createProduct(ProductRequest productRequest);

    void deleteProductById(Long id);

    List<Product> getAllProductByIds(BatchProductRequest request);
}
