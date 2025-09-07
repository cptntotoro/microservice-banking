package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.repository.user.UserRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .flatMap(userDao ->
                        userRoleRepository.findRoleNamesByUserUuid(userDao.getUuid())
                                .collectList()
                                .map(roles -> toUserDetails(userDao, roles))
                );
    }

    private UserDetails toUserDetails(UserDao userDao, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(userDao.getUsername())
                .password(userDao.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!userDao.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!userDao.isEnabled())
                .build();
    }
}