//package ru.practicum.config.security;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Component
//public class TokenHolder {
//
//    public Mono<String> getToken(ServerWebExchange exchange) {
//        return exchange.getSession()
//                .map(session -> (String) session.getAttributes().get("access_token"))
//                .defaultIfEmpty("");
//    }
//
//    public Mono<Void> setToken(ServerWebExchange exchange, String token) {
//        return exchange.getSession()
//                .doOnNext(session -> session.getAttributes().put("access_token", token))
//                .then();
//    }
//
//    public Mono<Void> clearToken(ServerWebExchange exchange) {
//        return exchange.getSession()
//                .doOnNext(session -> session.getAttributes().remove("access_token"))
//                .then();
//    }
//}