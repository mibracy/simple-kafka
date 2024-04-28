package org.example.controllers;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.example.data.ObjectDB;
import org.example.data.Users;
import org.example.service.FileExportService;
import org.example.sql.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.example.config.SecurityConfig.authHeaderCheck;

@Slf4j
@RestController()
public class SQLController {

    @Value("${bearer}")
    private String BEARER;
    private final UserRepository userRepo;
    private final SimpMessagingTemplate simp;
    private final SmartValidator validator;
    private final FileExportService exportService;

    @Autowired
    public SQLController(UserRepository userRepo, SimpMessagingTemplate simp,
                         SmartValidator validator, FileExportService exportService) {
        this.userRepo = userRepo;
        this.simp = simp;
        this.validator = validator;
        this.exportService = exportService;
    }

    @PostMapping("/sql/test_users")
    public ResponseEntity<String> testDBKafkaUsers(HttpServletRequest request) {
        // Check for Bearer Token & reject request if invalid
        var response = authHeaderCheck(request, BEARER);
//        if (response.getStatusCode().is4xxClientError()) {
//            return response;
//        }

        // store size of DB
        var beforeCount = userRepo.count();

        // mock Test users
        var users = new ArrayList<Users>();
        for (int i = 0; i < 10000; i++) {
            var faker = new Faker();
            var name = faker.futurama().character();
            var email = faker.dungeonsAndDragons().monsters();

            var userRandy = new Users(name, email+"@local.host");

            users.add(userRandy);
        }

        var errorCount = new AtomicInteger();
        // validate each H2User, save to correct DB -- errors out if invalid doesn't save
        users.forEach(user -> {
            var errors = new BeanPropertyBindingResult(user, user.getClass().getName());
            validator.validate(user, errors);

            if (!errors.hasErrors()) {
                userRepo.save(user);
            } else {
                errorCount.getAndIncrement();
            }
        });

//        userRepo.findAll().forEach(user -> logger.info(user.toString())); // dumps all to table to log

        // dump entire DB to file in JSON and XML formats
        var h2Repo = new ObjectDB(userRepo.findAll());
        exportService.export("data/users", h2Repo);

        // send size of DB to Kafka Broker for real-time display
        simp.convertAndSend("/topic/temperature", userRepo.count());

        // return Response in desired format
        response = new ResponseEntity<>("DB Size=" + beforeCount + "->" + userRepo.count() + " auto-rejected=" + errorCount.longValue(), HttpStatus.OK);
        return response;
    }

}