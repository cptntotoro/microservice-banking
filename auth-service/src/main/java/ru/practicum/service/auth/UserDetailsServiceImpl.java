//package ru.practicum.service.auth;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//import ru.practicum.client.UserClient;
//
///**
// * Кастомный ReactiveUserDetailsService для загрузки пользователей из account-service
// */
//@Service
//@RequiredArgsConstructor
//public class UserDetailsServiceImpl implements ReactiveUserDetailsService {
//
//    private final UserClient userClient;
//
//    @Override
//    public Mono<UserDetails> findByUsername(String username) {
//        return userClient.getUserByLogin(username)
//                .map(authUser -> (UserDetails) authUser)
//                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь с логином " + username + " не найден")));
//    }
//
//    public Mono<UserDetails> findByUserId(String userId) {
//        return userClient.getUserById(userId)
//                .map(authUser -> (UserDetails) authUser)
//                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь с ID " + userId + " не найден")));
//    }
//}
