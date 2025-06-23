package com.teatown.software.kubernetes.demo1.helloworld;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/hello-world")
@RestController
public class HelloWorldController {

    @GetMapping(path = "greetings", produces = "application/json")
    public ResponseEntity<String> greetings(@RequestParam(required = false) final String name, @AuthenticationPrincipal final User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (name == null || name.isEmpty()) {
            return ResponseEntity.ok("Hello World!");
        }

        return ResponseEntity.ok("Hello " + name + "!");
    }

}
