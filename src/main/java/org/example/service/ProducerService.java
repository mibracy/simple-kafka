package org.example.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.data.KafkaPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class ProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, String>> sendEvent(KafkaPayload event) {
        return kafkaTemplate.send(event.getTopic(), event.getKey() , event.getValue());
    }

    public void sendFirstCount() {
        var randomUUID = String.valueOf(UUID.randomUUID());

        for (int i = 1; i <= 10; i++) {
            var record = new ProducerRecord<>("first-count", randomUUID, String.valueOf(i));
            kafkaTemplate.send(record);
        }
    }
}