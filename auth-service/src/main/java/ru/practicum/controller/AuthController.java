//package ru.practicum.controller;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//import ru.practicum.dto.AuthRequest;
//import ru.practicum.dto.AuthResponse;
//import ru.practicum.dto.TokenValidationRequest;
//import ru.practicum.service.AuthService;
//
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final AuthService authService;
//
//    @PostMapping("/login")
//    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
//        return authService.authenticate(request.getUsername(), request.getPassword())
//                .map(ResponseEntity::ok)
//                .onErrorResume(e ->
//                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
//    }
//
//    @PostMapping("/validate")
//    public Mono<ResponseEntity<Boolean>> validateToken(@Valid @RequestBody TokenValidationRequest request) {
//        return authService.validateToken(request.getToken())
//                .map(ResponseEntity::ok)
//                .onErrorResume(e ->
//                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false)));
//    }
//
////    @GetMapping("/user/{userId}")
////    public Mono<ResponseEntity<Boolean>> validateUser(@PathVariable UUID userId) {
////        return authService.validateUser(userId.toString())
////                .map(ResponseEntity::ok)
////                .onErrorResume(e ->
////                        Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(false)));
////    }
//}