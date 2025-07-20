package com.teatown.software.kubernetes.demo.application.security;

import com.teatown.software.kubernetes.demo.helloworld.GreetingDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityConfigTest {

    private TestRestTemplate testRestTemplate;
    
    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        this.testRestTemplate = new TestRestTemplate();
    }

    @Test
    void whenApiIsRequested_andUserHeaderIsSetAndKnown_thenReturnOk_andGreeting() {
        final var name = "Marta";
        final var wantGreeting = String.format("Hello %s!", name);

        final var username = "testuser";
        final var headers = new HttpHeaders();
        // this header is necessary for authentication
        headers.add("user", username);

        final var httpEntity = new HttpEntity<>(headers);

        final ResponseEntity<GreetingDto> greetingDtoResponseEntity = invokeGreetingsApi(name, httpEntity);

        Assertions.assertEquals(HttpStatus.OK, greetingDtoResponseEntity.getStatusCode());

        final GreetingDto greetingDto = greetingDtoResponseEntity.getBody();
        Assertions.assertNotNull(greetingDto);
        Assertions.assertEquals(wantGreeting, greetingDto.getText());
    }

    private ResponseEntity<GreetingDto> invokeGreetingsApi(final String name, final HttpEntity<Object> httpEntity) {
        final var url = String.format("http://localhost:%s/api/hello-world/greetings?name=%s", this.port, name);
        return testRestTemplate.exchange(url, HttpMethod.GET, httpEntity, GreetingDto.class);
    }

    @Test
    void whenApiIsRequested_andUserHeaderIsNotSet_thenReturnForbidden() {
        final var name = "Marta";

        final var headers = new HttpHeaders();
        final var httpEntity = new HttpEntity<>(headers);

        final ResponseEntity<GreetingDto> greetingDtoResponseEntity = invokeGreetingsApi(name, httpEntity);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, greetingDtoResponseEntity.getStatusCode());
        Assertions.assertNull(greetingDtoResponseEntity.getBody());
    }

    @Test
    void whenApiIsRequested_andUserIsHeaderSetButEmpty_thenReturnForbidden() {
        final var name = "Marta";

        final var headers = new HttpHeaders();
        // this header is necessary for authentication
        headers.add("user", "");

        final var httpEntity = new HttpEntity<>(headers);

        final ResponseEntity<GreetingDto> greetingDtoResponseEntity = invokeGreetingsApi(name, httpEntity);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, greetingDtoResponseEntity.getStatusCode());
        Assertions.assertNull(greetingDtoResponseEntity.getBody());
    }

    @Test
    void whenApiIsRequested_andUserIsSetButUnknown_thenReturnForbidden() {
        final var name = "Marta";

        final var username = "unknownuser";
        final var headers = new HttpHeaders();
        // this header is necessary for authentication
        headers.add("user", username);

        final var httpEntity = new HttpEntity<>(headers);

        final ResponseEntity<GreetingDto> greetingDtoResponseEntity = invokeGreetingsApi(name, httpEntity);

        Assertions.assertEquals(HttpStatus.FORBIDDEN, greetingDtoResponseEntity.getStatusCode());
        Assertions.assertNull(greetingDtoResponseEntity.getBody());
    }
}
