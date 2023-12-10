package org.example.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.example.data.KafkaPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ProducerService producerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        producerService = new ProducerService(kafkaTemplate);
    }

    @Test
    public void testSendEvent() {
        KafkaPayload event = new KafkaPayload("topic", "key", "value");
        ListenableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(
                new SendResult<>(new ProducerRecord<>("topic", "key", "value"),
                        new RecordMetadata(new TopicPartition("topic", 1), 1, 1, 1, Long.valueOf(1), 1, 1)));
        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class))).thenReturn(future);

        producerService.sendEvent(event);

        verify(kafkaTemplate, times(1)).send(event.getTopic(), event.getKey(), event.getValue());
    }

    @Test
    public void testSendFirstCount() {
        producerService.sendFirstCount();

        verify(kafkaTemplate, times(10)).send(any(ProducerRecord.class));
    }
}
