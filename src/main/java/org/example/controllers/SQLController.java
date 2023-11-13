package org.example.controllers;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.example.data.H2User;
import org.example.data.ObjectDB;
import org.example.service.FileExportService;
import org.example.sql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.example.config.SecurityConfig.authHeaderCheck;

@Slf4j
@RestController()
public class SQLController {

    @Value("${bearer}")
    private String BEARER;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    SimpMessagingTemplate simp;

    @Autowired
    SmartValidator validator;

    @Autowired
    FileExportService exportService;

    @PostMapping("/sql/test_users")
    public ResponseEntity<String> testDBKafkaUsers(HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        ResponseEntity<String> response = authHeaderCheck(request, BEARER);
        if (response.getStatusCode().is4xxClientError()) {
            return response;
        }

        // store size of DB
        long beforeCount = userRepo.count();

        // mock Test users
        List<H2User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Faker faker = new Faker();
            String name = faker.funnyName().name();
            String email = faker.futurama().hermesCatchPhrase();

            H2User userRandy = new H2User(name, email+"@domain.com");

            users.add(userRandy);
        }

        // validate each H2User, save to correct DB -- errors out if invalid doesn't save
        users.forEach(user -> {
            BindingResult errors = new BeanPropertyBindingResult(user, user.getClass().getName());
            validator.validate(user, errors);

            if (!errors.hasErrors()) {
                userRepo.save(user);
            }
        });

//        userRepo.findAll().forEach(user -> logger.info(user.toString())); // dumps all to table to log

        // dump entire DB to file in JSON and XML formats
        Iterable<H2User> h2Repo = userRepo.findAll();
        exportService.export("data/users", Collections.singleton(h2Repo), new ObjectDB(h2Repo));

        // send size of DB to Kafka Broker for real-time display
        simp.convertAndSend("/topic/temperature", userRepo.count());

        // return Response in desired format
        response = new ResponseEntity<>("DB Size " + beforeCount + "->" + userRepo.count(), HttpStatus.OK);
        return response;
    }

}