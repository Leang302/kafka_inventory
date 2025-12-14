package com.leang.orderservice.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StockUpdateMessage {
    private List<StockUpdateItem> items;
    private String type;
    @Data
    @AllArgsConstructor
    public static class StockUpdateItem {
        private Long productId;
        private Integer quantity;
    }
}
