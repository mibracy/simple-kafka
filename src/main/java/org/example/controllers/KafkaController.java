package org.example.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.avro;
import org.example.avrokey;
import org.example.data.Event;
import org.example.data.ObjectDB;
import org.example.service.ProducerService;
import org.example.data.KafkaPayload;

import org.example.sql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;


import java.io.IOException;
import java.util.*;

import static org.example.config.SecurityConfig.authHeaderCheck;

@Slf4j
@RestController()
public class KafkaController {

    @Value("${bearer}")
    private String TOKEN;
    private final ProducerService producer;
    private final UserRepository eventRepo;
    private final SmartValidator validator;

    @Autowired
    public KafkaController(UserRepository eventRepo, ProducerService producer,
                           SmartValidator validator) {
        this.producer = producer;
        this.eventRepo = eventRepo;
        this.validator = validator;
    }

    @PostMapping("/api/send")
    public ResponseEntity<String> sendToKafkaTopic(@RequestBody String event, HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        var randomUUID = "-" + UUID.randomUUID();
        // send Event directly to 'my-topic' Kafka Broker without validation
//        producer.sendEvent(new KafkaPayload("my-topic", request.getHeader("key") + randomUUID, event.getBytes()));
        // return Response in desired format
        response = new ResponseEntity<>( "Event Generated UUID: " + request.getHeader("key") + randomUUID, HttpStatus.OK);
        return response;
    }

    @PostMapping("/api/avro")
    public ResponseEntity<String> recieveavro(@RequestBody String event, HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        var randomUUID = "-" + UUID.randomUUID();
        // send Event directly to 'my-topic' Kafka Broker without validation
//        producer.sendEvent(new KafkaPayload("avro", request.getHeader("key") + randomUUID, event.getBytes()));
        // return Response in desired format
        response = new ResponseEntity<>( "Event Generated UUID: " + request.getHeader("key") + randomUUID, HttpStatus.OK);
        return response;
    }

    @PostMapping("/api/schema")
    public ResponseEntity<String> sendArvoSchema(HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }
        Schema EventSch = ReflectData.get().getSchema(Event.class) ;
        Schema H2UserSch = ReflectData.get().getSchema(KafkaPayload.class) ;
        Schema KafkaPayloadSch = ReflectData.get().getSchema(KafkaPayload.class) ;
        Schema ObjectDBSch = ReflectData.get().getSchema(ObjectDB.class) ;

        response = new ResponseEntity<>("Event Schema : " + EventSch.toString()
                + "H2User Schema : " + H2UserSch.toString()
                + "KafkaPayload Schema : " + KafkaPayloadSch.toString()
                + "ObjectDB Schema : " + ObjectDBSch.toString()
                , HttpStatus.OK);

        return response;
    }

    @GetMapping("/api/avro")
    public ResponseEntity<String> sendArvoBytes(HttpServletRequest request) throws IOException {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
//            return response;
        }

        Gson gson = new Gson();
        Faker faker = new Faker();

        avro event = new avro(faker.futurama().quote());
        avrokey key = new avrokey(faker.princessBride().character());

        // send Event to Kafka Topic
        try {
            producer.sendEvent("avro", key, event);
        } catch (Exception e) {
            log.error("Oops -> {}",  e);
            return new ResponseEntity<>("Oops!", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Yippee-Ki-Yay !", HttpStatus.OK);
    }


    @PostMapping("/api/listen")
    public ResponseEntity<String> listenToKafkaTopic(@RequestBody String events, HttpServletRequest request) throws Exception {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // Remove any empty JSON elements sent, if any
        events = events.replace(",{}", "").replace("{}", "");

        // parse Event into Kafka Payload
        var mapper = new ObjectMapper();
        var result = mapper.readValue(events, new TypeReference<List<KafkaPayload>>(){});
        // validate each Event sent and relay to correct broker/db
        result.forEach(event -> {
            var errors = new BeanPropertyBindingResult(event, event.getClass().getName());
            validator.validate(event, errors);

            event.setKey(event.getTopic() +" ~!#~ "+ event.getKey()); 
            if (!errors.hasErrors()) {
                event.setTopic("kafka-sent-good");
//                producer.sendEvent(event); // Sends to Kafka Broker
//                eventRepo.save(new Event(event)); // Saves to DB
            } else {
                event.setTopic("error-sent-oops");
//                producer.sendEvent(event); // Sends error to Kafka Broker
//                eventRepo.save(new Event(event).isError()); // Saves error to DB
            }
        });

        return response;
    }

    @PostMapping("/api/publish")
    public ResponseEntity<String> publishToKafkaTopic(@RequestParam("message") String message, HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, TOKEN);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // send Event to Kafka Topic
//        producer.sendEvent(new KafkaPayload("temperature", String.valueOf(UUID.randomUUID()), message.getBytes()));

        // return Response in desired format
        response = new ResponseEntity<>( "Request Successful: " + request.getQueryString(), HttpStatus.OK);
        return response;
    }

    @GetMapping("/sql/topics")
    public ResponseEntity<List<String>> findTopics() {
        return new ResponseEntity<>(eventRepo.findDistinctTopic(), HttpStatus.OK);
    }

}