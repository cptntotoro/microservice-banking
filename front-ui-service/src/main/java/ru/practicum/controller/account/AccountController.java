package ru.practicum.controller.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.account.AddAccountRequestDto;
import ru.practicum.dto.account.DeleteAccountRequestDto;
import ru.practicum.service.account.AccountService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController extends BaseController {
    /**
     * Сервис пользователей
     */
    private final AccountService accountService;

    @PostMapping("/add-account")
    public Mono<String> addAccount(@ModelAttribute @Valid AddAccountRequestDto requestDto, ServerWebExchange exchange) {
        return withAuthenticatedUser(exchange, userId -> accountService.createAccount(userId, requestDto.getCurrencyCode())
                .flatMap(account -> {
                    log.info("Создание счета успешно выполнено: {}", account);
                    return encodeSuccessRedirect("dashboard", "Создание счета успешно выполнено");
                })
                .onErrorResume(e -> {
                    log.error("Создание счета провалено: {}", e.getMessage());
                    return encodeErrorRedirect("dashboard", "Создание счета провалено: " + e.getMessage());
                })
        );
    }

    @PostMapping("/delete-account")
    public Mono<String> deleteAccount(@ModelAttribute @Valid DeleteAccountRequestDto requestDto, ServerWebExchange exchange) {
        return withAuthenticatedUser(exchange, userId -> accountService.deleteAccount(userId, requestDto.getAccountId())
                .then(Mono.defer(() -> {
                    log.info("Удаление счета успешно выполнено: {}", requestDto.getAccountId());
                    return encodeSuccessRedirect("dashboard", "Удаление счета успешно выполнено");
                }))
                .onErrorResume(e -> {
                    log.error("Удаление счета провалено: {}", e.getMessage());
                    return encodeErrorRedirect("dashboard", "Удаление счета провалено: " + e.getMessage());
                })
        );
    }
}
