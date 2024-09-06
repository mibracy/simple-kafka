package org.example.sql;

import org.apache.poi.ss.formula.functions.T;
import org.example.data.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Table;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Repository
@Table(name = "USERS", schema = "SYSTEM")
public interface UserRepository extends CrudRepository<Users, Long> {

    @Query("""
            SELECT DISTINCT e.name 
            FROM Users e
            """)
    List<String> findDistinctTopic();
}