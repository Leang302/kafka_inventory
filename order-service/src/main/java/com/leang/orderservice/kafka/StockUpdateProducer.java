package com.leang.orderservice.kafka;

import com.leang.orderservice.kafka.message.StockUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockUpdateProducer {
    private final KafkaTemplate<String, StockUpdateMessage> kafkaTemplate;
    public void sendMessage(String topic, StockUpdateMessage message) {
        kafkaTemplate.send(topic, message);
    }

}
