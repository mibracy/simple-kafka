package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.example.data.KafkaPayload;
import org.example.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController()
@EnableScheduling
public class BatchController {

    private final ProducerService producer;
    private final SimpMessagingTemplate template;

    @Autowired
    public BatchController(ProducerService producer, SimpMessagingTemplate template) {
        this.producer = producer;
        this.template = template;
    }

    /*
     * Run at every minute:
     * Send message to Kafka
     * Log run to file
     */
    @Scheduled(cron = "0 * * * * *") // Every minute
    public void scheduleTaskUsingCronExpression() {
        // Format time for output UTC / Central / Dynamic
        Instant instant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
        String formattedUtcTime = formatter.format(instant);
        log.info("Formatted UTC time: " + formattedUtcTime);

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId centralZoneId = ZoneId.of("America/Chicago");
        ZonedDateTime centralTime = instant.atZone(centralZoneId);
        String formattedCentralTime = formatter.format(centralTime);
        log.info("Formatted Central time: " + formattedCentralTime);

        var randomUUID = "test-" + UUID.randomUUID();
        // Send Real-Time Update to display
        var payMap = new HashMap<String, String>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        payMap.put("topic", "cron" );
        payMap.put("key", randomUUID);
        payMap.put("value", mockJson() );
        payMap.put("time", sdf.format(new Date())); // Dynamic timezone option

        var kP = new KafkaPayload(payMap.get("topic"), payMap.get("key"), payMap.get("value"));

        producer.sendEvent(kP);
        template.convertAndSend("/topic/listen", payMap);

    }

    //  implementation 'com.google.code.gson:gson:2.10.1'
    //  implementation 'net.datafaker:datafaker:2.1.0'
    private String mockJson() {
        Gson gson = new Gson();
        Faker faker = new Faker();

        String jsonString = "{\n" +
                "    \"items\": [\n" +
                "        {\n" +
                "            \"item\": {\n" +
                "                \"id\": \"1\",\n" +
                "                \"code\": \"" + faker.number().randomNumber(6,true) +"\",\n" +
                "                \"description\": \"" + faker.food().dish() + "\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"item\": {\n" +
                "                \"id\": \"2\",\n" +
                "                \"code\": \"" + faker.number().randomNumber(13,true) + "\",\n" +
                "                \"description\": \"" + faker.science().tool() + "\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"ship_to_countries\": [\n" +
                "        \"" + faker.country().countryCode2().toUpperCase() + "\",\n" +
                "        \"" + faker.country().countryCode2().toUpperCase() + "\"\n" +
                "    ]\n" +
                "}";

        JsonElement je = gson.fromJson(jsonString, JsonElement.class);
        JsonObject jo = je.getAsJsonObject();

        JsonArray items = jo.getAsJsonArray("items");
        JsonArray countries = jo.getAsJsonArray("ship_to_countries");

        items.forEach(i -> log.info(i.toString()));
        countries.forEach(i -> log.info("country: " + i));

        return jo.toString();
    }

}
