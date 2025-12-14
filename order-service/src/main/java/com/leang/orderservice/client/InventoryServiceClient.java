package com.leang.orderservice.client;

import com.leang.orderservice.model.dto.request.BatchProductRequest;
import com.leang.orderservice.model.dto.response.ApiResponse;
import com.leang.orderservice.model.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "inventory-service", url = "http://localhost:8081/api/v1", path = "/products")
public interface InventoryServiceClient {
    @PostMapping("/batch")
    ResponseEntity<ApiResponse<List<Product>>> getAllProductsByIds(@RequestBody BatchProductRequest request);
}
