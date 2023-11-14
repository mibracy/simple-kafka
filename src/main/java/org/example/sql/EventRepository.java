package org.example.sql;

import org.example.data.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;
import java.util.List;

@Repository
@Table(name = "EVENT")
public interface EventRepository extends CrudRepository<Event, Long> {

    @Query("SELECT DISTINCT e.kafka_topic FROM Event e")
    List<String> findDistinctTopic();

}