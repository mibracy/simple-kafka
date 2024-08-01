package org.example.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.example.data.Aed;
import org.example.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
//    @Scheduled(cron = "30 * * * * *") // Every 30 seconds
//    @Scheduled(cron = "0 * * * * *") // Every minute
    public void scheduleTaskUsingCronExpression() throws UnknownHostException {
        // Format time for output UTC / Central / Dynamic
        var instant = Instant.now();
        var formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
        var formattedUtcTime = formatter.format(instant);
        log.info("Formatted UTC time: {}", formattedUtcTime);

        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        var centralZoneId = ZoneId.of("America/Chicago");
        var centralTime = instant.atZone(centralZoneId);
        var formattedCentralTime = formatter.format(centralTime);
        log.info("Formatted Central time: {}", formattedCentralTime);

        var randomUUID = InetAddress.getLocalHost().getHostName() +"-"+ UUID.randomUUID();

        // Send Real-Time Update to display
        var payMap = new HashMap<String, String>();
        var sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        payMap.put("topic", "cron" );
        payMap.put("key", randomUUID);
        payMap.put("value", mockJson() );
        payMap.put("time", sdf.format(new Date())); // Dynamic timezone option

//        var kP = new KafkaPayload(payMap.get("topic"), payMap.get("key"), payMap.get("value").getBytes());
//        java.net.Proxy proxy = new Proxy(Proxy.Type.HTTP,  new InetSocketAddress(proxyHost, proxyPort));
//        OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();
//
//        Retrofit.Builder builder = new Retrofit.Builder().client(client);
//        Retrofit retrofit = builder.build();
//        producer.sendEvent(kP);
    }

    @Value(value = "classpath:inputFiles.csv")
    private Resource csv;
    @Value(value = "classpath:dummy500.json")
    private Resource json;

//    @Scheduled(cron = "0 * * * * *") // Every 5 minutes
    public void scheduleTask() throws IOException {
        writeJsonOutput(generateJsonArray());
        echoCSV();
        jsonToKafka();
    }

    private void jsonToKafka() throws IOException {
        Aed score = new Aed();
        String name = "";
        try (JsonReader reader = new JsonReader(new FileReader(json.getFile()))) {
            log.debug("Start Array!");
            reader.beginArray();
            while (reader.peek() != JsonToken.END_DOCUMENT) {
                JsonToken nextToken = reader.peek();

                if (JsonToken.BEGIN_OBJECT.equals(nextToken)) {
                    reader.beginObject();
                } else if (JsonToken.NAME.equals(nextToken)) {
                    name = reader.nextName();
                    log.debug("Token KEY -> {}", name);
                } else if (JsonToken.STRING.equals(nextToken)) {
                    String value = reader.nextString();
                    if ("filename".equals(name)){
                        score.setName(value);
                    } else {
                        score.setDate(value);
                    }
                    log.debug("Token Value -> {}",  value);
                } else if (JsonToken.NUMBER.equals(nextToken)) {
                    long value = reader.nextLong();
                    log.debug("Token Value -> {}",  value);
                } else if (JsonToken.NULL.equals(nextToken)) {
                    reader.nextNull();
                    log.debug("Token Value -> null");
                } else if (JsonToken.END_OBJECT.equals(nextToken)) {
                    reader.endObject();
                    log.debug("Complete -> {}", score);
//                    KafkaPayload kP = new KafkaPayload("aed", score.getName(), score.getDate().getBytes());
//                    producer.sendEvent(kP);
                } else {
                    log.info("Finished Array!");
                    reader.endArray();
                }
            }
        }
        log.info("Finished Task!");
    }

    private void echoCSV() {
        try (Stream<String> lines = Files.lines(csv.getFile().toPath())) {
            lines.map(line -> line.split(","))
                    .forEach(columns -> {
                        log.info("Columns: {}", Arrays.toString(columns));
                    });
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void writeJsonOutput(JSONArray jsonObjects) throws IOException {
        try (FileWriter writer = new FileWriter("dummy.json")) {
            writer.write(jsonObjects.toJSONString());
        }
    }

    private JSONArray generateJsonArray() {
        Faker faker = new Faker();
        JSONArray jsonArray = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.debug("Starting JSON Gen!");

        for (int i = 0; i < 10000; i++) { // Generate 5 entries, you can adjust this number
            JSONObject jsonObject = new JSONObject();

            // Generate a random filename
            String filename = faker.file().fileName();

            // Generate a random date within the last year
            Date createdDate = faker.date().past(365, TimeUnit.DAYS);

            jsonObject.put("filename", filename);
            jsonObject.put("created_date", dateFormat.format(createdDate));

            jsonArray.add(jsonObject);
        }
        log.debug("Finished Generating!");

        return jsonArray;
    }

    // Example method
    private String getCreationDate(File file) throws IOException {
        Path filePath = file.toPath();
        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(attrs.creationTime().toMillis());
    }

    //  implementation 'com.google.code.gson:gson:2.10.1'
    //  implementation 'net.datafaker:datafaker:2.1.0'
    private String mockJson() {
        var gson = new Gson();
        var faker = new Faker();

        var jsonString = "{\n" +
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
                "        },\n" +
                "        {\n" +
                "            \"item\": {\n" +
                "                \"id\": \"3\",\n" +
                "                \"description\": \"" + faker.starTrek().klingon() + "\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"ship_to_countries\": [\n" +
                "        \"" + faker.country().countryCode2().toUpperCase() + "\",\n" +
                "        \"" + faker.country().countryCode2().toUpperCase() + "\"\n" +
                "    ]\n" +
                "}";

        var je = gson.fromJson(jsonString, JsonElement.class);
        var jo = je.getAsJsonObject();

        var items = jo.getAsJsonArray("items");
        var countries = jo.getAsJsonArray("ship_to_countries");

        items.forEach(i -> {
            JsonObject item = i.getAsJsonObject().get("item").getAsJsonObject();

            var id = String.valueOf(item.get("id"));
            var code = String.valueOf(item.get("code"));
            var description = String.valueOf(item.get("description"));

            if ("null".equals(code)) {
                log.info("#{} description: {}", id, description);
            } else {
                log.info("#{} code={} description: {}", id, code, description);
            }

        });
        countries.forEach(i -> log.info("country: {}", i.getAsString()));

        return jo.toString();
    }

}
