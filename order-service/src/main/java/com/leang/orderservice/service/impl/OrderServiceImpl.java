package com.leang.orderservice.service.impl;


import com.leang.orderservice.client.InventoryServiceClient;
import com.leang.orderservice.exception.NotFoundException;
import com.leang.orderservice.kafka.StockUpdateProducer;
import com.leang.orderservice.kafka.message.StockUpdateMessage;
import com.leang.orderservice.model.dto.request.BatchProductRequest;
import com.leang.orderservice.model.dto.request.OrderItemRequest;
import com.leang.orderservice.model.dto.request.OrderRequest;
import com.leang.orderservice.model.dto.response.ApiResponse;
import com.leang.orderservice.model.dto.response.OrderResponse;
import com.leang.orderservice.model.dto.response.PagedResponse;
import com.leang.orderservice.model.dto.response.PaginationInfo;
import com.leang.orderservice.model.entity.Order;
import com.leang.orderservice.model.entity.OrderItem;
import com.leang.orderservice.model.entity.Product;
import com.leang.orderservice.repository.OrderRepository;
import com.leang.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final StockUpdateProducer stockUpdateProducer;


    @Override
    public PagedResponse<OrderResponse> getAllOrder(Integer page, Integer size) {
        Page<Order> all = orderRepository.findAll(PageRequest.of(page - 1, size));
        //all product ids combine for all orders
        List<Long> productIds = all.getContent().stream().flatMap(order -> order.getOrderItems().stream()).map(OrderItem::getProductId).distinct().toList();
        //fetch all product for inventory service
        List<Product> products = Objects.requireNonNull(inventoryServiceClient.getAllProductsByIds(new BatchProductRequest(productIds)).getBody()).getPayload();
        //Map to OrderResponse
        List<OrderResponse> orderResponses = all.getContent().stream().map(order -> order.toResponse(products)).toList();
        return PagedResponse.<OrderResponse>builder().items(orderResponses).pagination(new PaginationInfo(all)).build();
    }

    @Override
    public OrderResponse getOrderById(Long id) {
        Order orderByIdInternal = getOrderByIdInternal(id);
        ResponseEntity<ApiResponse<List<Product>>> allProductsByIds = inventoryServiceClient.getAllProductsByIds(new BatchProductRequest(orderByIdInternal.getOrderItems().stream().map(OrderItem::getProductId).collect(Collectors.toList())));
        return orderByIdInternal.toResponse(Objects.requireNonNull(allProductsByIds.getBody()).getPayload());
    }

    @Override
    public Order getOrderByIdInternal(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order with id " + id + " not found."));
    }

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        List<Long> productIds = orderRequest.getOrderItems().stream().map(OrderItemRequest::getProductId).distinct().toList();
        List<Product> products = Objects.requireNonNull(inventoryServiceClient.getAllProductsByIds(new BatchProductRequest(productIds)).getBody()).getPayload();
        Order save = orderRepository.save(orderRequest.toEntity(products));
        List<StockUpdateMessage.StockUpdateItem> items = orderRequest.getOrderItems().stream()
                .map(i -> new StockUpdateMessage.StockUpdateItem(i.getProductId(), i.getQty()))
                .toList();
        stockUpdateProducer.sendMessage("stock-update-topic",new StockUpdateMessage(items,"DECREASE"));
        return getOrderById(save.getId());
    }
}
