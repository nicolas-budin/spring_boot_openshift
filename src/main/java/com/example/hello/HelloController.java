package com.example.hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class HelloController {

    @Value("${app.message}")
    private String message;

    @GetMapping("/")
    public String hello(Model model) {
        model.addAttribute("message", message);
        model.addAttribute("hostname", hostname());
        return "index";
    }

    @GetMapping("/api/hello")
    @ResponseBody
    public Map<String, Object> helloApi() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("hostname", hostname());
        return body;
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "inconnu";
        }
    }
}
