package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .authorizeExchange(exchanges -> exchanges
//                        // Разрешаем публичный доступ к эндпоинтам регистрации и аутентификации
//                        .pathMatchers(HttpMethod.POST, "/api/signup", "/api/login").permitAll()
//                        .pathMatchers(HttpMethod.GET, "/api/health", "/api/public/**").permitAll()
//                        // Защищаем все остальные эндпоинты
//                        .anyExchange().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .authenticationSuccessHandler(new DelegatingServerAuthenticationSuccessHandler(
//                                new RedirectServerAuthenticationSuccessHandler("/api/accounts")
//                        ))
//                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/login?error"))
//                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(authenticationManager())
                .securityContextRepository(securityContextRepository())
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((exchange, auth) -> {
                            ServerWebExchange swe = exchange.getExchange();

                            return swe.getSession()
                                    .doOnNext(WebSession::invalidate)
                                    .then(Mono.fromRunnable(() -> {
                                        swe.getResponse().setStatusCode(HttpStatus.OK);
                                    }));
                        })
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return Mono.empty();
                        })
                        .accessDeniedHandler((exchange, denied) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return Mono.empty();
                        })
                )
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}