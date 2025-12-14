package com.leang.orderservice.model.dto.request;

import com.leang.orderservice.exception.InsufficientStockException;
import com.leang.orderservice.model.entity.Order;
import com.leang.orderservice.model.entity.OrderItem;
import com.leang.orderservice.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

        List<String> errors = new ArrayList<>();
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : orderItems) {
            Product product = productMap.get(itemReq.getProductId());

            if (product == null) {
                errors.add("Product with ID " + itemReq.getProductId() + " not found");
                continue; // skip building item
            }

            if (product.getStock() < itemReq.getQty()) {
                errors.add(
                        String.format("Not enough stock for product '%s' (ID: %d). Requested: %d, Available: %d",
                                product.getName(),
                                product.getId(),
                                itemReq.getQty(),
                                product.getStock()
                        )
                );
            }

            // Only create item if product exists (even if stock is low, we still collect error but build item)
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .qty(itemReq.getQty())
                    .price(product.getPrice())
                    .build();

            items.add(orderItem);
        }

        if (!errors.isEmpty()) {
//            throw new InsufficientStockException(String.join("; ", errors));
        }

        order.setOrderItems(items);
        return order;
    }
}
