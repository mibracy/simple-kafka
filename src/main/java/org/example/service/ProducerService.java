package org.example.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.data.KafkaPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.UUID;

@Component
public class ProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public ListenableFuture<SendResult<String, String>> sendEvent(KafkaPayload event) {
        return kafkaTemplate.send(event.getTopic(), event.getKey() , event.getValue());
    }

    public void sendFirstCount() {
        String randomUUID = String.valueOf(UUID.randomUUID());

        for (int i = 1; i <= 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>("first-count", randomUUID, String.valueOf(i));
            kafkaTemplate.send(record);
        }
    }
}