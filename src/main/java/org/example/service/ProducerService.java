package org.example.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.avro;
import org.example.avrokey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ProducerService {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    public ProducerService(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(String topic, Object key, Object event) {
       ProducerRecord<Object, Object> producerRecord = new ProducerRecord<>(topic, key, event);
       CompletableFuture<SendResult<Object,Object>> completeFuture = kafkaTemplate.send(producerRecord);
       log.info("Send event to kafka on topic {}", producerRecord.topic());

       completeFuture.whenComplete((result, ex) -> {
           if (ex == null) {
               log.info("Kafka message successfully sent on topic {} and value {}", producerRecord.topic(), result.getProducerRecord().value().toString());
           } else {
               log.warn("An error occurred while sending kafka message for event with value {}", producerRecord);
           }
        });

    }
}
