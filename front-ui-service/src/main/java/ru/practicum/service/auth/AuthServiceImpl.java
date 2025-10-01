package ru.practicum.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.account.SignUpResponseClientDto;
import ru.practicum.client.auth.AuthServiceClient;
import ru.practicum.client.auth.TokenResponseDto;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final AccountServiceClient accountServiceClient;
    private final AuthServiceClient authServiceClient;


    @Override
    public Mono<SignUpResponseClientDto> createAccount(SignUpRequestDto signUpRequestDto) {
        return accountServiceClient.createAccount(signUpRequestDto);
    }

    @Override
    public Mono<TokenResponseDto> login(LoginRequestDto loginRequest) {
        return authServiceClient.login(loginRequest);
    }

    @Override
    public void logout(WebSession session) {
        session.getAttributes().remove("access_token");
        session.getAttributes().remove("refresh_token");
    }
}
