package com.leang.orderservice.model.entity;

import com.leang.orderservice.model.dto.response.OrderItemResponse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "order_items")
@Table
public class OrderItem extends BaseEntityAudit {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private Long productId;
    private Integer qty;
    private BigDecimal price;

    public OrderItemResponse toResponse(Product product) {
        return OrderItemResponse.builder()
                .id(id)
                .productId(productId)
                .productName(product != null ? product.getName() : null)
                .qty(qty)
                .price(price)
                .build();
    }
}
