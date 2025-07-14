package com.teatown.software.kubernetes.demo.application.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public RequestHeaderAuthenticationFilter authenticationFilter() {
        final var filter = new RequestHeaderAuthenticationFilter();

        filter.setAuthenticationManager(authenticationManagerProviderList());

        // this commands SpringSecurity which http request header to look for during authentication
        filter.setPrincipalRequestHeader("user");
        filter.setExceptionIfHeaderMissing(false);

        return filter;
    }

    private AuthenticationManager authenticationManagerProviderList() {
        return new ProviderManager(Collections.singletonList(userProvider()));
    }

    private PreAuthenticatedAuthenticationProvider userProvider() {
        final var provider = new PreAuthenticatedAuthenticationProvider();

        provider.setThrowExceptionWhenTokenRejected(true);
        provider.setPreAuthenticatedUserDetailsService(authenticationUserDetailService());

        return provider;
    }

    private AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailService() {
        return new UserDetailsByNameServiceWrapper<>(userDetailsService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> new User("testuser", "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // to avoid cross-origin source resource sharing errors,
    // it is necessary to add CORS mappings
    // so that the HTTP response headers Access-Control-Allow-* are added by the Spring Boot backend
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/hello-world/greetings").allowedOrigins("http://localhost:4200");
            }
        };
    }

    @Bean
    public RestTemplate restTemplate() {
        final var restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(Collections.singletonList(new MappingJackson2HttpMessageConverter()));
        return restTemplate;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.GET, "/isAlive" ).permitAll()
                        // to avoid cross-origin source resource sharing errors,
                        // it is necessary to allow the browser's preflight HTTP OPTIONS request,
                        // so that the HTTP response headers Access-Control-Allow-* are added by the Spring Boot backend
                        //
                        // another way to combat this is to add HTTP headers via browser plugin to every HTTP request.
                        // in this case, there's no need to permit the HTTP OPTIONS preflight w/o authentication
                        .requestMatchers(HttpMethod.OPTIONS, "/api/hello-world/greetings").permitAll()
                        // to become authorized, a principal (e.g. user, device, system)
                        // must have the authority ROLE_USER assigned
                        .requestMatchers(HttpMethod.GET, "/api/hello-world/greetings").hasAuthority("ROLE_USER")
                        // the matcher below ensures that any new API that is not matched by the request matchers above
                        // will be secured by default
                        .anyRequest().authenticated()
                )
                // see https://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/html5/#csrf
                .csrf(AbstractHttpConfigurer::disable)
                // make sure the authentication filter is applied before Spring Security tries
                // to authorize the request with the lambda passed into authorizeRequests()
                .addFilter(authenticationFilter());

        return http.build();
    }
}
