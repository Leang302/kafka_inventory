package com.leang.orderservice.service;

import com.leang.orderservice.model.dto.request.OrderRequest;
import com.leang.orderservice.model.dto.response.OrderResponse;
import com.leang.orderservice.model.dto.response.PagedResponse;
import com.leang.orderservice.model.entity.Order;
import jakarta.validation.constraints.Positive;

public interface OrderService {
    PagedResponse<OrderResponse> getAllOrder(@Positive Integer page, @Positive Integer size);

    OrderResponse getOrderById(Long id);
    Order getOrderByIdInternal(Long id);
    OrderResponse createOrder(OrderRequest orderRequest);

    OrderResponse updateOrderById(Long id, OrderRequest orderRequest);

    void deleteOrderById(Long id);
}
