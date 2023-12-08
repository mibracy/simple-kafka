package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.data.KafkaPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Component
public class ConsumerService {
    private final SimpMessagingTemplate template;

    @Autowired
    public ConsumerService(SimpMessagingTemplate template) {
        this.template = template;
    }

    @KafkaListener(topics = "my-topic")
    public void genericListen(String event) {
        log.info("New event: " + event);
    }

    @KafkaListener(topics="temperature")
    public void consume(@Payload String value) {
        try {
            Double temp = Double.parseDouble(value);
            template.convertAndSend("/topic/temperature", temp);
        } catch (NumberFormatException e) {
            log.error("Invalid format sent. Must be Integer/Double value");
        }
    }

    @KafkaListener(topics="first-count")
    public void consume(ConsumerRecord<String, KafkaPayload> payload) {
        Map<String, String> payMap = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        payMap.put("key", String.valueOf(payload.key()));
        payMap.put("topic", payload.topic());
        payMap.put("value", String.valueOf(payload.value()));
        payMap.put("time", sdf.format(payload.timestamp()));

        template.convertAndSend("/topic/listen", payMap);
    }

}