package ru.practicum.controller.cash;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.cash.DepositWithdrawCashRequestDto;
import ru.practicum.mapper.cash.CashMapper;
import ru.practicum.service.auth.AuthService;
import ru.practicum.service.cash.CashService;

import java.util.Collections;

@Controller
@RequestMapping("/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController extends BaseController {
    /**
     * Сервис обналичивания денег
     */
    private final CashService cashService;

    /**
     * Маппер операций с наличными
     */
    private final CashMapper cashMapper;


    @PostMapping
    public Mono<String> cashOperation(@ModelAttribute @Valid DepositWithdrawCashRequestDto requestDto, ServerWebExchange exchange, Model model) {
        log.info("Обработка запроса операции со счетом: operation={}, сurrency={}, сумма={}",
                requestDto.getOperation(), requestDto.getCurrency(), requestDto.getAmount());

        return withAuthenticatedUser(exchange, userId -> cashService.cashOperation(userId, requestDto)
                .map(cashMapper::cashToCashResponseDto)
                .flatMap(response -> {
                    if ("ERROR".equals(response.getStatus())) {
                        log.warn("Ошибка операции {} со счетом: {}", requestDto.getOperation(), response.getMessage());
                        model.addAttribute("cashErrors", Collections.singletonList(response.getMessage()));
                        return renderPage(model, "page/dashboard", "Главная страница",
                                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                    }
                    log.info("Операция {} со счетом успешно выполнено: {}", requestDto.getOperation(), response.getMessage());
                    model.addAttribute("cashSuccess", response.getMessage());
                    return encodeSuccessRedirect("dashboard", "Операция " + requestDto.getOperation() + " со счетом успешно выполнено");
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Неверный запрос на операцию {} со счетом: {}", requestDto.getOperation(), e.getMessage());
                    model.addAttribute("cashErrors", Collections.singletonList(e.getMessage()));
                    return renderPage(model, "page/dashboard", "Главная страница",
                            "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Непредвиденная ошибка при операции {} со счетом: {}", requestDto.getOperation(), e.getMessage(), e);
                    model.addAttribute("cashErrors", Collections.singletonList("Внутренняя ошибка сервера"));
                    return renderPage(model, "page/dashboard", "Главная страница",
                            "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                })
        );
    }
}