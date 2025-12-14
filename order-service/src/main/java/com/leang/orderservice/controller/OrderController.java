package com.leang.orderservice.controller;


import com.leang.orderservice.model.dto.request.OrderRequest;
import com.leang.orderservice.model.dto.response.ApiResponse;
import com.leang.orderservice.model.dto.response.OrderResponse;
import com.leang.orderservice.model.dto.response.PagedResponse;
import com.leang.orderservice.service.OrderService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController extends BaseController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllProducts(@RequestParam(defaultValue = "1") @Positive Integer page, @RequestParam(defaultValue = "10") @Positive Integer size) {
        return responseEntity("All orders retrieved successfully", orderService.getAllOrder(page, size));
    }

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        return responseEntity("Order by id retrieved successfully", orderService.getOrderById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody OrderRequest orderRequest) {
        return responseEntity("Order created successfully", HttpStatus.CREATED, orderService.createOrder(orderRequest));
    }

    @PutMapping("{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderById(@RequestBody OrderRequest orderRequest, @PathVariable Long id) {
        return responseEntity("Order created successfully", orderService.updateOrderById(id,orderRequest));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> deleteOrderById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return responseEntity("Order deleted successfully");
    }


}
