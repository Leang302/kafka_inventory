package com.leang.orderservice.model.dto.response;

import com.leang.orderservice.model.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Order order;
    private Long productId;
    private String productName;
    private Integer qty;
    private BigDecimal price;
}
