package com.ecommerce.gateway.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(authorize -> authorize
                    .pathMatchers("/api/users/auth").permitAll()
                    .pathMatchers("/api/products/**").hasRole("PRODUCT")
                    .pathMatchers("/api/orders/**").hasRole("ORDER")
                    .pathMatchers("/api/users/**").hasRole("USER")
                    .anyExchange().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                            .jwtAuthenticationConverter(grantedAuthoritiesExtract())
                    )
            )
        ;
        return http.build();
    }

//    Đây là một hàm trả về một Converter:
//    Đầu vào: Jwt (JWT token đã được xác thực).
//    Đầu ra: Mono<AbstractAuthenticationToken> —
//    một token bảo mật có danh sách quyền (authorities), dùng trong WebFlux.
//
//    ***Example of using EntrySet with Map
//    Map<String, String> users = Map.of(
//            "u1", "Alice",
//            "u2", "Bob",
//            "u3", "Charlie"
//    );
//    users.entrySet().stream()
//        .forEach(entry -> {
//            System.out.println("Key: " + entry.getKey());
//            System.out.println("Value: " + entry.getValue());
//        });
//
//    ***Example of using flatMap with List
//    .flatMap(...)
//    Với entry này, lấy ra value: entry.getValue() → là Map<String, List<String>>
//            (cụ thể là: { "roles": ["admin", "user"] })
//    Lấy "roles" → List<String>, rồi gọi .stream() trên list đó.
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtract() {
        ReactiveJwtAuthenticationConverter jwtAuthenticationConverter =
                new ReactiveJwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsMap("resource_access")
                    .entrySet().stream()
                    .filter(entry -> entry.getKey().equals("oauth2-pkce"))
                    .flatMap(entry -> ((Map<String, List<String>>) entry.getValue()).get("roles").stream())
                    .toList();
            System.out.println("Extracted roles: " + roles);
            return Flux.fromIterable(roles)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
        });

        return jwtAuthenticationConverter;
    }
}
