package org.example.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

//    CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1;
//    https://www.h2database.com/html/commands.html
}
