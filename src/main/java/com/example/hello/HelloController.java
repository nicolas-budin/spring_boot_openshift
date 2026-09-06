package com.example.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello World depuis Spring Boot sur OpenShift !";
    }

    @GetMapping("/api/hello")
    public Map<String, Object> helloApi() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Hello World");
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
