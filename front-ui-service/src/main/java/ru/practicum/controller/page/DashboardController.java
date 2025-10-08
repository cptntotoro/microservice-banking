package ru.practicum.controller.page;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.account.AddAccountRequestDto;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.dto.user.UserDashboardDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.auth.AuthService;
import ru.practicum.service.exchange.ExchangeRateService;
import ru.practicum.service.user.UserService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController extends BaseController {
    /**
     * Сервис курсов обмена валют
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Маппер операций с наличными
     */
    private final ExchangeRateMapper exchangeRateMapper;

    /**
     * Сервис пользователей
     */
    private final UserService userService;
    private final AuthService authService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public Mono<String> showDashboard(ServerWebExchange exchange, Model model) {
        return exchange.getSession().flatMap(session -> {
            log.error("111111111111111111");
            log.error((String) session.getAttributes().get("access_token"));
            log.error(session.getAttributes().values().toString());
            if (session.getAttributes().get("access_token") == null) {
                return Mono.just("redirect:/login");
//                return Mono.error(new IllegalStateException("access_token is null"));
            }
            return authService.getUserId((String) session.getAttributes().get("access_token"))
                    .flatMap(userId -> {
                        log.info("Юзер загружен: " + userId);
                        if (userId == null) {
                            return Mono.just("redirect:/login");
                        }

                        return userService.getUserWithAccounts(UUID.fromString(userId))
                                .map(userWithAccounts -> {
                                    log.info("Аккаунт загружен: " + userWithAccounts);
                                    return userMapper.toUserDashboardDto(userWithAccounts);
                                })
                                .flatMap(userDashboardDto -> {
                                    model.addAttribute("user", userDashboardDto);
                                    model.addAttribute("userAccounts", userDashboardDto.getAccounts());

                                    // Инициализируем формы для переводов
                                    model.addAttribute("ownTransferRequest", new OwnTransferRequestDto());
                                    model.addAttribute("otherTransferRequest", new OtherTransferRequestDto());

                                    // Обработка сообщений
                                    String error = exchange.getRequest().getQueryParams().getFirst("error");
                                    String success = exchange.getRequest().getQueryParams().getFirst("success");

                                    if (error != null) {
                                        model.addAttribute("error", error);
                                    }
                                    if (success != null) {
                                        model.addAttribute("success", success);
                                    }

                                    log.info("Загружаем валюты");

                                    // Загружаем курсы валют
                                    return Mono.zip(exchangeRateService.getCurrentRates().collectList(), exchangeRateService.getAvailableCurrencies().collectList())
                                            .flatMap(tuple2 -> {
                                                List<ExchangeRateDto> ratesDto = exchangeRateMapper.toExchangeRateDtoList(tuple2.getT1());
                                                model.addAttribute("rates", ratesDto);
                                                log.info("Загружено {} курсов валют для отображения", ratesDto.size());

                                                model.addAttribute("currencies", tuple2.getT2());

                                                List<String> userCurrencies = tuple2.getT2().stream().filter(currency ->
                                                        userDashboardDto.getAccounts().stream().anyMatch(
                                                                accountDashboardDto -> accountDashboardDto.getCurrencyCode().equals(currency))).toList();
                                                model.addAttribute("userCurrencies", userCurrencies);
                                                model.addAttribute("userNotHaveCurrencies", tuple2.getT2().stream().filter(currency -> !userCurrencies.contains(currency)));
                                                log.error("222222222");
                                                log.error("Загружено {} валют", tuple2.getT2());

                                                return renderPage(model, "page/dashboard", "Главная страница",
                                                        "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                                            })
                                            .onErrorResume(e -> {
                                                log.warn("Не удалось загрузить курсы валют: {}", e.getMessage());
                                                model.addAttribute("rates", List.of());
                                                model.addAttribute("error", "Не удалось загрузить курсы валют: " + e.getMessage());
                                                return renderPage(model, "page/dashboard", "Главная страница",
                                                        "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                                            });
                                })
                                .onErrorResume(e -> {
                                    log.error("Ошибка загрузки данных пользователя: {}", e.getMessage());
                                    model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
                                    model.addAttribute("user", UserDashboardDto.builder().build());
                                    model.addAttribute("userAccounts", List.of());
                                    model.addAttribute("rates", List.of());

                                    return renderPage(model, "page/dashboard", "Главная страница",
                                            "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                                });
                    });
        });
    }

    @PostMapping("/add-account")
    public Mono<String> addAccount(@ModelAttribute AddAccountRequestDto requestDto, ServerWebExchange exchange) {
        return exchange.getSession().flatMap(session -> {
            log.error("111111111111111111");
            log.error("currencyCode: {}", requestDto.getCurrencyCode());
            log.error("access_token: {}", (String) session.getAttributes().get("access_token"));
            if (session.getAttributes().get("access_token") == null) {
                return Mono.just("redirect:/login");
//                return Mono.error(new IllegalStateException("access_token is null"));
            }
            return authService.getUserId((String) session.getAttributes().get("access_token"))
                    .flatMap(userId -> {
                        if (userId == null) {
                            return Mono.just("redirect:/login");
                        }
                        log.error("111111111 Создание счета: {}, {}", userId, requestDto.getCurrencyCode());
                        return userService.createAccount(UUID.fromString(userId), requestDto.getCurrencyCode())
                                .flatMap(account -> {
                                    log.info("Создание счета успешно выполнено: {}", account);
                                    return Mono.just("redirect:/dashboard?success=Создание счета успешно выполнено");
                                })
                                .onErrorResume(e -> {
                                    log.error("Создание счета провалено: {}", e.getMessage());
                                    return Mono.just("redirect:/dashboard?error=Создание счета провалено: " + e.getMessage());
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Ошибка загрузки данных пользователя: {}", e.getMessage());
                        return Mono.just("redirect:/dashboard?error=Ошибка загрузки данных пользователя: " + e.getMessage());
                    });
        })
        .onErrorResume(e -> {
            log.error("Ошибка загрузки: {}", e.getMessage());
            return Mono.just("redirect:/dashboard?error=Ошибка загрузки данных: " + e.getMessage());
        });
    }
}
