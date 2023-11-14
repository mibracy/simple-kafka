package org.example.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@XStreamAlias("user")
public class H2User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    private String name;

    @Email
    private String email;


    public H2User(String name, String email) {
        this.name = name;
        this.email = email;
    }

//    CREATE TABLE H2USER(ID INT PRIMARY KEY, NAME VARCHAR(255), EMAIL VARCHAR(255));

//    CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1;
//    https://www.h2database.com/html/commands.html
}
