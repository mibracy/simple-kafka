package org.example.controllers;


import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {

    private final SimpMessagingTemplate template;
    AtomicInteger hitCnt = new AtomicInteger();

    @Autowired
    public HomeController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @GetMapping("/home")
    public String home(Model model, @RequestParam(value = "source", defaultValue = "GET") String sourceLoad) {
        model.addAttribute("sourceLoad", sourceLoad);
        return "home";
    }
    @PostMapping("/landing")
    public String landing(HttpServletRequest req, @RequestBody String body, Model model) throws UnsupportedEncodingException {
        // Check for Auth
        log.debug(req.getHeader("Authorization"));

        // Process request
        Gson gson = new Gson();
        Map map = gson.fromJson(body, Map.class);
        log.info("manifest:" + map.get("manifest"));

        // Send Real-Time Update to display
        Map payMap = new HashMap<String, String>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        payMap.put("topic", req.getMethod() );
        payMap.put("key", String.valueOf(map.get("manifest")));
        payMap.put("value", String.valueOf(map.get("note")));
        payMap.put("time", sdf.format(new Date()));

        template.convertAndSend("/topic/listen", payMap);
        template.convertAndSend("/topic/temperature", hitCnt.getAndIncrement());

        return "redirect:/home?source=" + URLEncoder.encode(req.getMethod(), String.valueOf(StandardCharsets.UTF_8))
                               + "&id=" + URLEncoder.encode((String) map.get("manifest"), String.valueOf(StandardCharsets.UTF_8));
    }

}
