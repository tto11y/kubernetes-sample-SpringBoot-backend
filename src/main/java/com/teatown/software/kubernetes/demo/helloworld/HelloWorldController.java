package com.teatown.software.kubernetes.demo.helloworld;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/hello-world")
@RestController
public class HelloWorldController {

    @GetMapping(path = "greetings", produces = "application/json")
    public ResponseEntity<GreetingDto> greetings(@RequestParam(required = false) final String name, @AuthenticationPrincipal final User user) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (name == null || name.isEmpty()) {
            return ResponseEntity.ok(GreetingDto.builder().text("Hello World!").build());
        }

        return ResponseEntity.ok(GreetingDto.builder().text("Hello " + name + "!").build());
    }
}
