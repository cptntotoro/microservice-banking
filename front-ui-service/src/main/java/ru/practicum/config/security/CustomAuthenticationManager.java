//package ru.practicum.config.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.ReactiveAuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import ru.practicum.client.user.auth.AuthServiceClient;
//import ru.practicum.dto.auth.LoginRequestDto;
//
//import java.util.Collections;
//
//@Component
//@RequiredArgsConstructor
//public class CustomAuthenticationManager implements ReactiveAuthenticationManager {
//
//    private final AuthServiceClient authServiceClient;
//    private final ServerWebExchange exchange;
//
//    @Override
//    public Mono<Authentication> authenticate(Authentication authentication) {
//        String username = authentication.getName();
//        String password = authentication.getCredentials().toString();
//
//        LoginRequestDto loginRequest = new LoginRequestDto();
//        loginRequest.setLogin(username);
//        loginRequest.setPassword(password);
//
//        return authServiceClient.login(loginRequest)
//                .flatMap(loginResponse -> {
//                    // Сохраняем токен в сессии
//                    exchange.getSession().subscribe(session -> {
//                        session.getAttributes().put("access_token", loginResponse.getAccessToken());
//                        session.getAttributes().put("user_profile", loginResponse.getUserProfile());
//                    });
//
//                    // Создаем аутентификацию для Spring Security
//                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
//                            username,
//                            password,
//                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//                    );
//                    auth.setDetails(loginResponse);
//
//                    return Mono.just(auth);
//                })
//                .onErrorResume(e -> Mono.error(new org.springframework.security.core.AuthenticationException("Invalid credentials") {}));
//    }
//}
