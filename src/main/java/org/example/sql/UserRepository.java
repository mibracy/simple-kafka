package org.example.sql;

import org.example.data.H2User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "H2USER")
public interface UserRepository extends CrudRepository<H2User, Long> {}