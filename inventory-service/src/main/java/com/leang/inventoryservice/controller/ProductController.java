package com.leang.inventoryservice.controller;

import com.leang.inventoryservice.model.dto.request.BatchProductRequest;
import com.leang.inventoryservice.model.dto.request.ProductRequest;
import com.leang.inventoryservice.model.dto.response.ApiResponse;
import com.leang.inventoryservice.model.dto.response.PagedResponse;
import com.leang.inventoryservice.model.entity.Product;
import com.leang.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController extends BaseController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> getAllProducts(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {
        return responseEntity("All products retrieved successfully", productService.getAllProducts(page, size));
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return responseEntity("Product by id retrieved successfully", productService.getProductById(id));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProductsByIds(@RequestBody BatchProductRequest request) {
        return responseEntity("All products by Ids retrieved successfully", productService.getAllProductByIds(request));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody ProductRequest productRequest) {
        return responseEntity("Product created successfully", HttpStatus.CREATED, productService.createProduct(productRequest));
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<Product>> updateProductById(@PathVariable Long id, @RequestBody ProductRequest productRequest) {
        return responseEntity("Product by id updated successfully", productService.updateProductById(id, productRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Product>> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return responseEntity("Product by id updated successfully");
    }

}
