package org.example.data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "event_GEN")
    @SequenceGenerator(name = "event_GEN", sequenceName = "event_SEQ", allocationSize = 1)
    private long id;
    private String kafka_topic;
    private String kafka_key;
    private String kafka_value;
    private Boolean error = false;

    public Event(KafkaPayload payload) {
       this.kafka_topic = payload.getTopic();
       this.kafka_key= payload.getKey();
       this.kafka_value = payload.getValue();
    }

    public Event isError() {
        this.error = true;
        return this;
    }


//    CREATE TABLE EVENT(ID INT PRIMARY KEY, KAFKA_TOPIC VARCHAR(255), KAFKA_KEY VARCHAR(255), KAFKA_VALUE VARCHAR(255), ERROR BOOLEAN);

//    CREATE SEQUENCE EVENT_SEQ START WITH 1 INCREMENT BY 1;
//    https://www.h2database.com/html/commands.html
}
