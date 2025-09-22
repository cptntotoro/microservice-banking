//package ru.practicum.filter;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import ru.practicum.dto.TokenValidationRequest;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter implements GlobalFilter {
//
//    private final WebClient webClient;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String path = exchange.getRequest().getURI().getPath();
//
//        // Пропускаем публичные endpoints
//        if (path.startsWith("/auth/") || path.startsWith("/actuator/")) {
//            return chain.filter(exchange);
//        }
//
//        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//            return exchange.getResponse().setComplete();
//        }
//
//        String token = authHeader.substring(7);
//
//        return webClient.post()
//                .uri("http://auth-service/auth/validate")
//                .bodyValue(new TokenValidationRequest(token))
//                .retrieve()
//                .bodyToMono(Boolean.class)
//                .flatMap(isValid -> {
//                    if (Boolean.TRUE.equals(isValid)) {
//                        return chain.filter(exchange);
//                    } else {
//                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                        return exchange.getResponse().setComplete();
//                    }
//                })
//                .onErrorResume(e -> {
//                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                    return exchange.getResponse().setComplete();
//                });
//    }
//}
