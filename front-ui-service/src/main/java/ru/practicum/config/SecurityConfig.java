package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;
import ru.practicum.service.auth.AuthService;
import ru.practicum.service.jwtvalidation.JwtValidationService;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtValidationService jwtValidationService;

    private final AuthService authService;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login", "/signup", "/fragments/**", "/templates/**", "/styles/**", "/scripts/**", "/images/**").permitAll()
                        .pathMatchers("/dashboard","/logout").authenticated()
                        .anyExchange().authenticated()
                )

                .addFilterAt(authenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)

//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessHandler((exchange, authentication) -> {
//                            // Очистка сессии
//                            return exchange.getExchange().getSession()
//                                    .flatMap(session -> {
//                                        session.getAttributes().clear();
//                                        return Mono.empty();
//                                    })
//                                    .then(Mono.just("redirect:/login?logout").then());
//                        })
//                )

                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((exchange, ex) -> {
                            return Mono.fromRunnable(() -> {
                                exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                exchange.getResponse().getHeaders().setLocation(
                                        java.net.URI.create("/login?error=unauthorized")
                                );
                            });
                        })
                )

                .build();
    }

    @Bean
    public AuthenticationWebFilter authenticationWebFilter() {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager());
        filter.setServerAuthenticationConverter(exchange -> {
            // Получаем токен из сессии
            return exchange.getSession()
                    .flatMap(session -> {
                        String token = (String) session.getAttributes().get("access_token");
                        if (token == null || token.isBlank()) {
                            return Mono.empty();
                        }
                        return jwtValidationService.validateToken(token)
                                .doOnError(throwable -> authService.logout(session))
                                .map(claims -> {
                                    String username = claims.getSubject();
                                    List<String> roles = claims.get("roles", List.class);
                                    List<SimpleGrantedAuthority> authorities = roles != null ?
                                            roles.stream()
                                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                                    .collect(Collectors.toList()) :
                                            List.of(new SimpleGrantedAuthority("ROLE_USER"));

                                    return new UsernamePasswordAuthenticationToken(
                                            username, null, authorities
                                    );
                                });
                    });
        });

        return filter;
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                return Mono.just(authentication);
            }
            return Mono.empty();
        };
    }
}