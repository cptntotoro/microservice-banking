//package ru.practicum.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//import reactor.util.context.Context;
//
//@Configuration
//@RequiredArgsConstructor
//public class FeignContextFilter implements WebFilter {
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        return exchange.getSession()
//                .flatMap(session -> {
//                    // Получаем токен из сессии
//                    String token = (String) session.getAttributes().get("access_token");
//
//                    // Сохраняем токен в атрибутах exchange для доступа в Feign interceptor
//                    if (token != null) {
//                        exchange.getAttributes().put("FEIGN_AUTH_TOKEN", token);
//                    }
//
//                    // Создаем Reactor Context с токеном
//                    Context context = Context.empty();
//                    if (token != null) {
//                        context = context.put("AUTH_TOKEN", token);
//                    }
//
//                    return chain.filter(exchange)
//                            .contextWrite(context);
//                });
//    }
//}