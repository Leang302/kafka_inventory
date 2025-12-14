package com.leang.orderservice.model.dto.request;

import com.leang.orderservice.model.entity.Order;
import com.leang.orderservice.model.entity.OrderItem;
import com.leang.orderservice.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String customerName;
    private Boolean status;
    private List<OrderItemRequest> orderItems;

    public Order toEntity(List<Product> products) {
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Order order = Order.builder()
                .customerName(customerName)
                .status(status)
                .build();
        List<OrderItem> items = orderItems.stream().map(itemReq -> {
                    Product product = productMap.get(itemReq.getProductId());
                    return OrderItem.builder()
                            .order(order)
                            .productId(product.getId())
                            .qty(itemReq.getQty())
                            .price(product.getPrice())
                            .build();
                })
                .toList();
        order.setOrderItems(items);
        return order;
    }
}
