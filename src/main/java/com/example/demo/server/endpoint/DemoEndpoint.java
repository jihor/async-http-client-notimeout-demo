package com.example.demo.server.endpoint;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author jihor (jihor@ya.ru)
 * Created on 16.05.2018
 */
@RestController
public class DemoEndpoint {

    @RequestMapping("/test")
    public ResponseEntity<String> test(@RequestParam("name") String name) throws InterruptedException {
        for (int i = 1; i <= 600; i++) {
            TimeUnit.SECONDS.sleep(1);
            if (i % 30 == 0) {
//                System.out.println("Server slept for " + i + " seconds for " + name);
            }
        }
//        System.out.println(name + ": server sleep complete");
        return ResponseEntity.ok("Done");
    }
}
