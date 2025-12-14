package com.leang.orderservice.service.impl;


import com.leang.orderservice.client.InventoryServiceClient;
import com.leang.orderservice.exception.InsufficientStockException;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
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
        System.out.println(new BatchProductRequest(orderByIdInternal.getOrderItems().stream().map(OrderItem::getProductId).collect(Collectors.toList())));
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
        //old list
        List<Product> products = Objects.requireNonNull(inventoryServiceClient.getAllProductsByIds(new BatchProductRequest(productIds)).getBody()).getPayload();
        Order save = orderRepository.save(orderRequest.toEntity(products));
        List<StockUpdateMessage.StockUpdateItem> items = orderRequest.getOrderItems().stream()
                .map(i -> new StockUpdateMessage.StockUpdateItem(i.getProductId(), i.getQty()))
                .toList();
        stockUpdateProducer.sendMessage("stock-update-topic",new StockUpdateMessage(items,"DECREASE"));
        return getOrderById(save.getId());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderById(Long id, OrderRequest orderRequest) {
        // 1. Fetch existing order
        Order order = getOrderByIdInternal(id); // includes orderItems

        // 2. Prepare quantity maps
        Map<Long, Integer> oldQty = order.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItem::getProductId, OrderItem::getQty, Integer::sum));

        Map<Long, Integer> newQty = orderRequest.getOrderItems().stream()
                .collect(Collectors.toMap(OrderItemRequest::getProductId, OrderItemRequest::getQty, Integer::sum));

        // 3. Fetch all relevant products
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(oldQty.keySet());
        allProductIds.addAll(newQty.keySet());

        Map<Long, Product> products = Objects.requireNonNull(
                inventoryServiceClient.getAllProductsByIds(new BatchProductRequest(new ArrayList<>(allProductIds)))
                        .getBody()
        ).getPayload().stream().collect(Collectors.toMap(Product::getId, p -> p));

        // 4. Calculate stock changes
        List<StockUpdateMessage.StockUpdateItem> toDecrease = new ArrayList<>();
        List<StockUpdateMessage.StockUpdateItem> toIncrease = new ArrayList<>();

        for (Long pid : allProductIds) {
            int oldQ = oldQty.getOrDefault(pid, 0);
            int newQ = newQty.getOrDefault(pid, 0);
            int diff = newQ - oldQ;

            Product product = products.get(pid);
            if (product == null) throw new IllegalArgumentException("Product not found: " + pid);

            if (diff > 0) {
                if (product.getStock() + oldQ < newQ) {
                    throw new IllegalArgumentException("Not enough stock for product " + pid);
                }
                toDecrease.add(new StockUpdateMessage.StockUpdateItem(pid, diff));
            } else if (diff < 0) {
                toIncrease.add(new StockUpdateMessage.StockUpdateItem(pid, -diff));
            }
        }

        // 5. Update order
        order.setCustomerName(orderRequest.getCustomerName());
        order.setStatus(orderRequest.getStatus());

        order.getOrderItems().clear();
        for (OrderItemRequest itemReq : orderRequest.getOrderItems()) {
            Product product = products.get(itemReq.getProductId());
            order.getOrderItems().add(OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .qty(itemReq.getQty())
                    .price(product.getPrice())
                    .build()
            );
        }

        Order saved = orderRepository.save(order);

        // 6. Send stock update messages
        if (!toDecrease.isEmpty()) stockUpdateProducer.sendMessage("stock-update-topic", new StockUpdateMessage(toDecrease, "DECREASE"));
        if (!toIncrease.isEmpty()) stockUpdateProducer.sendMessage("stock-update-topic", new StockUpdateMessage(toIncrease, "INCREASE"));

        return getOrderById(saved.getId());
    }


    @Override
    public void deleteOrderById(Long id) {
        Order order = getOrderByIdInternal(id);  // should be fetched with orderItems
        List<StockUpdateMessage.StockUpdateItem> items = order.getOrderItems().stream()
                .map(i -> new StockUpdateMessage.StockUpdateItem(i.getProductId(), i.getQty()))
                .toList();
        stockUpdateProducer.sendMessage("stock-update-topic",new StockUpdateMessage(items,"INCREASE"));
    }
}
