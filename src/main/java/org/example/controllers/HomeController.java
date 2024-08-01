package org.example.controllers;


import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {

    private final SimpMessagingTemplate template;

    @Autowired
    public HomeController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/home")
    public String home(HttpServletRequest req, HttpServletResponse resp,  @AuthenticationPrincipal OidcUser principal,
                       Model model, @RequestParam(value = "manifest", defaultValue = "init") String chef) {

        model.addAttribute("user", (principal != null) ? principal.getFullName() : "");
        return "home";
    }

}
