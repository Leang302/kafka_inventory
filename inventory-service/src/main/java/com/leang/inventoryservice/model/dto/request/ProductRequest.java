package com.leang.inventoryservice.model.dto.request;

import com.leang.inventoryservice.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequest {
    private String name;
    private Integer stock;
    private BigDecimal price;

    public Product toEntity() {
        return Product.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .build();
    }
}
