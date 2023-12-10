package org.example.controllers;

import org.example.data.KafkaPayload;
import org.example.service.ProducerService;
import org.example.sql.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.SmartValidator;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class KafkaControllerTest {

    @Mock
    private ProducerService producerService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private SmartValidator validator;
    @Mock
    private HttpServletRequest request;

    private KafkaController kafkaController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        kafkaController = new KafkaController(eventRepository, producerService, validator);
    }

    @Test
    public void testSendToKafkaTopic() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(producerService.sendEvent(any(KafkaPayload.class))).thenReturn(null);

        String response = kafkaController.sendToKafkaTopic("test event", request);

        assertEquals("Event Generated UUID: null", response);
        verify(producerService, times(1)).sendEvent(any(KafkaPayload.class));
    }

    @Test
    public void testSendSqlTopic() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");

        String response = kafkaController.sendSqlTopic(request);

        assertEquals("Request Successful", response);
        verify(producerService, times(1)).sendFirstCount();
    }

    @Test
    public void testListenToKafkaTopic() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(validator.validate(any(KafkaPayload.class), any())).thenReturn(true);
        when(producerService.sendEvent(any(KafkaPayload.class))).thenReturn(null);

        String response = kafkaController.listenToKafkaTopic("test events", request);

        assertEquals("Request Successful", response);
        verify(producerService, times(1)).sendEvent(any(KafkaPayload.class));
        verify(eventRepository, times(1)).save(any());
    }

    @Test
    public void testPublishToKafkaTopic() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(producerService.sendEvent(any(KafkaPayload.class))).thenReturn(null);

        String response = kafkaController.publishToKafkaTopic("test message", request);

        assertEquals("Request Successful: null", response);
        verify(producerService, times(1)).sendEvent(any(KafkaPayload.class));
    }

    @Test
    public void testFindTopics() {
        when(eventRepository.findDistinctTopic()).thenReturn(Arrays.asList("topic1", "topic2"));

        String response = kafkaController.findTopics();

        assertEquals("[topic1, topic2]", response);
        verify(eventRepository, times(1)).findDistinctTopic();
    }
}
