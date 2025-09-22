//package ru.practicum.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import ru.practicum.config.JwtConfig;
//import ru.practicum.config.JwtUtil;
//import ru.practicum.dto.AuthResponse;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final ReactiveUserDetailsService userDetailsService;
//    private final JwtUtil jwtUtil;
//    private final JwtConfig jwtConfig;
//    private final PasswordEncoder passwordEncoder;
//
//    public Mono<AuthResponse> authenticate(String username, String password) {
//        return userDetailsService.findByUsername(username)
//                .filter(userDetails -> passwordEncoder.matches(password, userDetails.getPassword()))
//                .flatMap(userDetails -> {
//                    String token = jwtUtil.generateToken(userDetails);
//                    return Mono.just(new AuthResponse(token, jwtConfig.getExpiration()));
//                })
//                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")));
//    }
//
//    public Mono<Boolean> validateToken(String token) {
//        return Mono.just(jwtUtil.validateToken(token));
//    }
//
//    public Mono<Authentication> getAuthentication(String token) {
//        if (jwtUtil.validateToken(token)) {
//            String username = jwtUtil.extractUsername(token);
//            return userDetailsService.findByUsername(username)
//                    .map(userDetails ->
//                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
//        }
//        return Mono.empty();
//    }
//
//    public Mono<String> getUsernameFromToken(String token) {
//        if (jwtUtil.validateToken(token)) {
//            return Mono.just(jwtUtil.extractUsername(token));
//        }
//        return Mono.empty();
//    }
//}