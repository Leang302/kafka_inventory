package com.leang.orderservice.model.entity;

import com.leang.orderservice.model.dto.response.OrderResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "orders")
@Table
public class Order extends BaseEntityAudit {
    @Id
    @GeneratedValue
    private Long id;
    private String customerName;
    private Boolean status;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    public OrderResponse toResponse(List<Product> products) {
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(
                Product::getId, Function.identity()
        ));
        return OrderResponse.builder()
                .id(id)
                .customerName(customerName)
                .status(status)
                .orderItems(
                        orderItems.stream()
                                .map(item ->
                                        item.toResponse(productMap.get(item.getProductId()))
                                )
                                .toList()
                )
                .build();
    }
}
