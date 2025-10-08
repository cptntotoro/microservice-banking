package ru.practicum.controller.cash;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.practicum.dto.cash.CashRequestDto;
import ru.practicum.mapper.cash.CashMapper;
import ru.practicum.service.cash.CashService;

import java.util.Collections;

@Controller
@RequestMapping("/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {
    /**
     * Сервис обналичивания денег
     */
    private final CashService cashService;

    /**
     * Маппер операций с наличными
     */
    private final CashMapper cashMapper;

    @PostMapping("/deposit")
    public Mono<String> deposit(@Valid @RequestBody CashRequestDto requestDto, Model model) {
        log.info("Обработка запроса на пополнение счета: accountId={}, сумма={}",
                requestDto.getAccountId(), requestDto.getAmount());

        return cashService.deposit(cashMapper.cashRequestDtoToCash(requestDto))
                .map(cashMapper::cashToCashResponseDto)
                .map(response -> {
                    if ("ERROR".equals(response.getStatus())) {
                        log.warn("Ошибка пополнения счета: {}", response.getMessage());
                        model.addAttribute("cashErrors", Collections.singletonList(response.getMessage()));
                        return "page/dashboard";
                    }
                    log.info("Пополнение счета успешно выполнено: {}", response.getMessage());
                    model.addAttribute("cashSuccess", response.getMessage());
                    return "redirect:/dashboard?success=" + response.getMessage();
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Неверный запрос на пополнение счета: {}", e.getMessage());
                    model.addAttribute("cashErrors", Collections.singletonList(e.getMessage()));
                    return Mono.just("page/dashboard");
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Непредвиденная ошибка при пополнении счета: {}", e.getMessage(), e);
                    model.addAttribute("cashErrors", Collections.singletonList("Внутренняя ошибка сервера"));
                    return Mono.just("page/dashboard");
                });
    }

    @PostMapping("/withdraw")
    public Mono<String> withdraw(@Valid @RequestBody CashRequestDto requestDto, Model model) {
        log.info("Обработка запроса на снятие средств: accountId={}, сумма={}",
                requestDto.getAccountId(), requestDto.getAmount());

        return cashService.withdraw(cashMapper.cashRequestDtoToCash(requestDto))
                .map(cashMapper::cashToCashResponseDto)
                .map(response -> {
                    if ("ERROR".equals(response.getStatus())) {
                        log.warn("Ошибка снятия средств: {}", response.getMessage());
                        model.addAttribute("cashErrors", Collections.singletonList(response.getMessage()));
                        return "page/dashboard";
                    }
                    log.info("Снятие средств успешно выполнено: {}", response.getMessage());
                    model.addAttribute("cashSuccess", response.getMessage());
                    return "redirect:/dashboard?success=" + response.getMessage();
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Неверный запрос на снятие средств: {}", e.getMessage());
                    model.addAttribute("cashErrors", Collections.singletonList(e.getMessage()));
                    return Mono.just("page/dashboard");
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Непредвиденная ошибка при снятии средств: {}", e.getMessage(), e);
                    model.addAttribute("cashErrors", Collections.singletonList("Внутренняя ошибка сервера"));
                    return Mono.just("page/dashboard");
                });
    }
}