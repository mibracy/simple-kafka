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
    AtomicInteger hitCnt = new AtomicInteger();
    ConcurrentHashMap<String, String> sessionRef = new ConcurrentHashMap<>();

    @Autowired
    public HomeController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/home")
    public String home(HttpServletRequest req, HttpServletResponse resp,  @AuthenticationPrincipal OidcUser principal,
                       Model model, @RequestParam(value = "manifest", defaultValue = "init") String chef) throws IOException {

        model.addAttribute("user", (principal != null) ? principal.getFullName() : "");
        return "home";
    }

    @PostMapping("/landing")
    public String landing(HttpServletRequest req, HttpServletResponse resp,
                          @RequestBody String body, Model model) throws IOException {
        // Check for Auth
        if (req.getHeader("Authorization") == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Requires Authentication");
            return null; // return null to indicate your program that the request has finished with the error.
        }

        // Process request
        Gson gson = new Gson();
        Map map = gson.fromJson(body, Map.class);
        log.info("manifest:" + map.get("manifest"));
        map.remove("note");

        // Send Real-Time Update to display
        Map payMap = new HashMap<String, String>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        payMap.put("topic", req.getMethod() );
        payMap.put("key", String.valueOf(map.get("manifest")));
        payMap.put("value", String.valueOf(map.get("note")));
        payMap.put("time", sdf.format(new Date()));

        template.convertAndSend("/topic/listen", payMap);
        template.convertAndSend("/topic/temperature", hitCnt.getAndIncrement());

        model.addAttribute("sourceLoad", String.valueOf(map.get("manifest")));

        return "redirect:/home?manifest="
                + URLEncoder.encode((String) map.get("manifest"), String.valueOf(StandardCharsets.UTF_8));
    }

}
