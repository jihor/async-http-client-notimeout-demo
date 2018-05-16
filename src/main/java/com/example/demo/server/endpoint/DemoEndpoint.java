package com.example.demo.server.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
@RestController
public class DemoEndpoint {

    @RequestMapping("/test")
    public ResponseEntity<String> test() throws InterruptedException {
        for (int i = 1; i <= 600; i++) {
            TimeUnit.SECONDS.sleep(1);
        }
        return ResponseEntity.ok("Done");
    }
}
