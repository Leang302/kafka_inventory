package com.leang.inventoryservice.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMessage {
    private List<StockUpdateItem> items;
    private String type;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockUpdateItem {
        private Long productId;
        private Integer quantity;
    }
}