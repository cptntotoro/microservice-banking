//package ru.practicum.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;
//import reactivefeign.ReactiveFeign;
//import reactivefeign.ReactiveFeignBuilder;
//import reactivefeign.ReactiveOptions;
//import reactivefeign.client.ReactiveHttpClientFactory;
//import reactor.core.publisher.Mono;
//
//import java.util.function.Supplier;
//
//@Component
//@Primary
//public class CustomReactiveFeignBuilder extends ReactiveFeign.Builder {
//
//    @Autowired
//    public CustomReactiveFeignBuilder(FeignConfig feignConfig) {
//        super(ReactorFeign.builder()
//                .encoder(feignConfig.feignEncoder())
//                .decoder(feignConfig.feignDecoder())
//                .errorDecoder(feignConfig.feignErrorDecoder())
//                .decode404());
//    }
//
//    @Override
//    protected ReactiveHttpClientFactory clientFactory() {
//        return null;
//    }
//
//    @Override
//    public ReactiveFeignBuilder objectMapper(ObjectMapper objectMapper) {
//        return null;
//    }
//
//    @Override
//    public ReactiveFeignBuilder options(ReactiveOptions reactiveOptions) {
//        return null;
//    }
//}