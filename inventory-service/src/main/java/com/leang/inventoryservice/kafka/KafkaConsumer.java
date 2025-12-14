package com.leang.inventoryservice.kafka;

import com.leang.inventoryservice.kafka.message.StockUpdateMessage;
import com.leang.inventoryservice.model.entity.Product;
import com.leang.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ProductRepository productRepository;

    @KafkaListener(topics = "stock-update-topic", groupId = "inventory-service-group-v2")
    public void consume(StockUpdateMessage message) {
        message.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            if ("DECREASE".equalsIgnoreCase(message.getType())) {
                product.setStock(product.getStock() - item.getQuantity());
            } else if ("INCREASE".equalsIgnoreCase(message.getType())) {
                product.setStock(product.getStock() + item.getQuantity());
            }
            productRepository.save(product);
        });
    }
}
