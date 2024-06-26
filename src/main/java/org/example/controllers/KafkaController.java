package org.example.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.data.Event;
import org.example.service.ProducerService;
import org.example.data.KafkaPayload;

import org.example.sql.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.example.config.SecurityConfig.authHeaderCheck;

@Slf4j
@RestController()
public class KafkaController {

    @Value("${bearer}")
    private String TOKEN;
    private final ProducerService producer;
    private final EventRepository eventRepo;
    private final SmartValidator validator;

    @Autowired
    public KafkaController(EventRepository eventRepo, ProducerService producer,
                           SmartValidator validator) {
        this.producer = producer;
        this.eventRepo = eventRepo;
        this.validator = validator;
    }

    @PostMapping("/api/send")
    public ResponseEntity<String> sendToKafkaTopic(@RequestBody String event, HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        ResponseEntity<String> response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        String randomUUID = "-" + UUID.randomUUID();
        // send Event directly to 'my-topic' Kafka Broker without validation
        producer.sendEvent(new KafkaPayload("my-topic", request.getHeader("key")+ randomUUID, event));

        // return Response in desired format
        response = new ResponseEntity<>( "Event Generated UUID: " + request.getHeader("key") + randomUUID, HttpStatus.OK);
        return response;
    }

    @PostMapping("/api/beginning")
    public ResponseEntity<String> sendSqlTopic(HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        ResponseEntity<String> response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // send default values to 'sql' Kafka topic
        producer.sendFirstCount();

        return response;
    }


    @PostMapping("/api/listen")
    public ResponseEntity<String> listenToKafkaTopic(@RequestBody String events, HttpServletRequest request) throws Exception {
        // Check for Bearer Token & reject request if invalid
        ResponseEntity<String> response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // Remove any empty JSON elements sent, if any
        events = events.replace(",{}", "").replace("{}", "");

        // parse Event into Kafka Payload
        ObjectMapper mapper = new ObjectMapper();
        List<KafkaPayload> result = mapper.readValue(events, new TypeReference<List<KafkaPayload>>(){});
        // validate each Event sent and relay to correct broker/db
        result.forEach(event -> {
            BindingResult errors = new BeanPropertyBindingResult(event, event.getClass().getName());
            validator.validate(event, errors);

            event.setKey(event.getTopic() +" ~!#~ "+ event.getKey()); 
            if (!errors.hasErrors()) {
                event.setTopic("kafka-sent-good");
                producer.sendEvent(event); // Sends to Kafka Broker
                eventRepo.save(new Event(event)); // Saves to DB
            } else {
                event.setTopic("error-sent-oops");
                producer.sendEvent(event); // Sends error to Kafka Broker
                eventRepo.save(new Event(event).isError()); // Saves error to DB
            }
        });

        return response;
    }

    @PostMapping("/api/publish")
    public ResponseEntity<String> publishToKafkaTopic(@RequestParam("message") String message, HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        ResponseEntity<String> response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // send Event to Kafka Topic
        producer.sendEvent(new KafkaPayload("temperature", String.valueOf(UUID.randomUUID()), message));

        // return Response in desired format
        response = new ResponseEntity<>( "Request Successful: " + request.getQueryString(), HttpStatus.OK);
        return response;
    }

    @GetMapping("/sql/topics")
    public ResponseEntity<List<String>> findTopics() {
        return new ResponseEntity<>(eventRepo.findDistinctTopic(), HttpStatus.OK);
    }

}