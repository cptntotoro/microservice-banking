//package ru.practicum.config;
//
//import org.springframework.stereotype.Component;
//import reactivefeign.ReactiveFeign;
//import reactor.core.publisher.Mono;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class ReactiveFeignContext {
//
//    private final Map<Class<?>, Object> clients = new ConcurrentHashMap<>();
//    private final Map<String, ReactiveFeign.Builder> builders = new ConcurrentHashMap<>();
//
//    @SuppressWarnings("unchecked")
//    public <T> T getClient(Class<T> type, ReactiveFeign.Builder builder, String url) {
//        return (T) clients.computeIfAbsent(type, key -> builder.target(type, url));
//    }
//
//    public void addBuilder(String name, ReactiveFeign.Builder builder) {
//        builders.put(name, builder);
//    }
//
//    public ReactiveFeign.Builder getBuilder(String name) {
//        return builders.get(name);
//    }
//
//    public Mono<Void> clearCache() {
//        clients.clear();
//        builders.clear();
//        return Mono.empty();
//    }
//}