//package ru.practicum.config;
//
//import feign.Feign;
//import feign.RequestInterceptor;
//import feign.codec.Decoder;
//import feign.codec.Encoder;
//import feign.codec.ErrorDecoder;
//import feign.jackson.JacksonDecoder;
//import feign.jackson.JacksonEncoder;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import reactivefeign.ReactiveFeign;
//
//import java.util.concurrent.ConcurrentHashMap;
//
//@Configuration
//@RequiredArgsConstructor
//public class FeignConfig {
//
//    private final ServerWebExchange exchange;
//
//    @Bean
//    public WebFilter feignTokenFilter() {
//        return (exchange, chain) -> {
//            return exchange.getSession()
//                    .flatMap(session -> {
//                        String token = (String) session.getAttributes().get("access_token");
//                        if (token != null) {
//                            exchange.getAttributes().put("feign_token", token);
//                        }
//                        return chain.filter(exchange);
//                    });
//        };
//    }
//
//    @Bean
//    public RequestInterceptor requestInterceptor() {
//        return template -> {
//            // Получаем токен из атрибутов обмена (устанавливается в фильтре)
//            String token = (String) exchange.getAttribute("FEIGN_AUTH_TOKEN");
//            if (token != null && !token.isEmpty()) {
//                template.header("Authorization", "Bearer " + token);
//            }
//
//            // Добавляем общие заголовки
//            template.header("Content-Type", "application/json");
//            template.header("Accept", "application/json");
//            template.header("X-Request-Source", "front-ui-service");
//        };
//    }
//
//    private String getTokenFromContext() {
//        // Реализация получения токена из контекста
//        // Можно использовать Reactor Context или атрибуты ServerWebExchange
//        return null;
//    }
//
//    @Bean
//    public Encoder feignEncoder() {
//        return new JacksonEncoder();
//    }
//
//    @Bean
//    public Decoder feignDecoder() {
//        return new JacksonDecoder();
//    }
//
//    @Bean
//    public ErrorDecoder feignErrorDecoder() {
//        return (methodKey, response) -> {
//            if (response.status() == HttpStatus.UNAUTHORIZED.value()) {
//                return new RuntimeException("Неавторизованный доступ");
//            }
//            if (response.status() == HttpStatus.NOT_FOUND.value()) {
//                return new RuntimeException("Ресурс не найден");
//            }
//            if (response.status() >= 400 && response.status() < 500) {
//                return new RuntimeException("Ошибка клиента: " + response.status());
//            }
//            if (response.status() >= 500) {
//                return new RuntimeException("Ошибка сервера: " + response.status());
//            }
//            return new RuntimeException("Ошибка при вызове сервиса");
//        };
//    }
//
//    @Bean
//    public Feign.Builder feignBuilder(Encoder encoder, Decoder decoder, ErrorDecoder errorDecoder) {
//        return ReactorFeign.builder()
//                .encoder(encoder)
//                .decoder(decoder)
//                .errorDecoder(errorDecoder)
//                .decode404();
//    }
//
//    @Bean
//    @Primary
//    public ReactiveFeign.Builder reactiveFeignBuilder(Feign.Builder feignBuilder) {
//        return ReactiveFeign.fromFeignBuilder(feignBuilder);
//    }
//
//    // Кэш для Feign клиентов
//    @Bean
//    public ConcurrentHashMap<Class<?>, Object> feignClientCache() {
//        return new ConcurrentHashMap<>();
//    }
//}