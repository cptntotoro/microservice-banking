package ru.practicum.controller.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.user.UserDashboardDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.account.AccountService;
import ru.practicum.service.exchange.ExchangeRateService;

import java.util.List;

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
    private final AccountService accountService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public Mono<String> showDashboard(ServerWebExchange exchange, Model model) {
        return withAuthenticatedUser(exchange, userId -> accountService.getUserWithAccounts(userId)
                .map(userWithAccounts -> {
                    log.info("Аккаунт загружен: " + userWithAccounts);
                    return userMapper.toUserDashboardDto(userWithAccounts);
                })
                .flatMap(userDashboardDto -> {
                    model.addAttribute("user", userDashboardDto);
                    model.addAttribute("userAccounts", userDashboardDto.getAccounts());

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
                })
        );
    }
}
