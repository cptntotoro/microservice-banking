//package ru.practicum.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import ru.practicum.client.user.auth.AuthServiceClient;
//import ru.practicum.client.user.auth.LoginResponseDto;
//import ru.practicum.dto.auth.LoginRequestDto;
//import ru.practicum.dto.auth.SignUpRequestDto;
//import ru.practicum.dto.auth.SignUpResponseDto;
//
//@Service
//@RequiredArgsConstructor
//public class AuthService {
//
//    private final AuthServiceClient authServiceClient;
//
//    public Mono<LoginResponseDto> login(LoginRequestDto loginRequest) {
//        return authServiceClient.login(loginRequest);
//    }
//
//    public Mono<SignUpResponseDto> signup(SignUpRequestDto signupRequest) {
//        return authServiceClient.signup(signupRequest);
//    }
//
////    public Mono<Void> logout(String token) {
////        return authServiceClient.logout("Bearer " + token);
////    }
//}