package org.example.data;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@XStreamAlias("user")
public class H2User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "h2user_GEN")
    @SequenceGenerator(name = "h2user_GEN", sequenceName = "h2user_SEQ", allocationSize = 1)
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

//    CREATE SEQUENCE H2USER_SEQ START WITH 1 INCREMENT BY 1;
//    https://www.h2database.com/html/commands.html
}
