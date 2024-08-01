package org.example.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class ConsumerService {
    private final SimpMessagingTemplate template;
    private final SimpleDateFormat sdf;

    private static final String TOPIC_TEMPERATURE = "/topic/temperature";
    private static final String TOPIC_LISTEN = "/topic/listen";

    @Autowired
    public ConsumerService(SimpMessagingTemplate template) {
        this.template = template;
        this.sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    public Map<String, String> handle(ConsumerRecord<Object, Object> payload) {
        var payMap = new HashMap<String, String>();
        payMap.put("key", String.valueOf(payload.key()));
        payMap.put("topic", payload.topic());
        payMap.put("value", String.valueOf(payload.value()));
        payMap.put("time", sdf.format(payload.timestamp()));
        return payMap;
    }

    @KafkaListener(topics = "avro")
    public void consumeAvro(ConsumerRecord<Object, Object> payload) {
        Map<String, String> payMap = null;
        payMap = handle(payload);

        template.convertAndSend(TOPIC_TEMPERATURE, payload.offset());
        if (payMap != null) {
            template.convertAndSend(TOPIC_LISTEN, payMap);
        }
    }
}
