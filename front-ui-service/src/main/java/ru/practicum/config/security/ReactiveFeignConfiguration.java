//package ru.practicum.config.security;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import reactivefeign.ReactiveFeign;
//import ru.practicum.config.FeignConfig;
//import ru.practicum.config.ReactiveFeignContext;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Configuration
//@ConditionalOnClass({ReactiveFeign.class, FeignClient.class})
//public class ReactiveFeignConfiguration {
//
//    @Bean
//    @ConditionalOnMissingBean
//    public ReactiveFeign.Builder reactiveFeignBuilder(FeignConfig feignConfig) {
//        return feignConfig.reactiveFeignBuilder(feignConfig.feignBuilder(
//                feignConfig.feignEncoder(),
//                feignConfig.feignDecoder(),
//                feignConfig.feignErrorDecoder()
//        ));
//    }
//
//    @Bean
//    public Map<String, ReactiveFeign.Builder> reactiveFeignBuilders() {
//        return new ConcurrentHashMap<>();
//    }
//
//    @Bean
//    public ReactiveFeignContext reactiveFeignContext() {
//        return new ReactiveFeignContext();
//    }
//}