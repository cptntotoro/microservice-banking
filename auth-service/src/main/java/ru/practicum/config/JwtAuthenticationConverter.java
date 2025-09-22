//package ru.practicum.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpHeaders;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import ru.practicum.service.AuthService;
//
//@RequiredArgsConstructor
//public class JwtAuthenticationConverter implements ServerAuthenticationConverter {
//
//    private final AuthService authService;
//
//    @Override
//    public Mono<Authentication> convert(ServerWebExchange exchange) {
//        return Mono.justOrEmpty(exchange.getRequest())
//                .flatMap(request -> {
//                    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
//
//                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                        String token = authHeader.substring(7);
//                        return authService.getAuthentication(token);
//                    }
//                    return Mono.empty();
//                });
//    }
//}