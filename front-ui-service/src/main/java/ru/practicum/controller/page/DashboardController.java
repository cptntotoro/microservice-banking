package ru.practicum.controller.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.user.LoginResponseClientDto;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.dto.user.UserDashboardDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.exchange.ExchangeRateService;
import ru.practicum.service.user.UserService;

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
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public Mono<String> showDashboard(ServerWebExchange exchange, Model model) {
        return exchange.getSession().flatMap(session -> {
            LoginResponseClientDto userData = (LoginResponseClientDto) session.getAttributes().get("userData");

            if (userData == null) {
                return Mono.just("redirect:/login");
            }

            return userService.getUserWithAccounts(userData.getUserId())
                    .map(userMapper::toUserDashboardDto)
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

                        // Загружаем курсы валют
                        return exchangeRateService.getCurrentRates()
                                .collectList()
                                .flatMap(rates -> {
                                    List<ExchangeRateDto> ratesDto = exchangeRateMapper.toExchangeRateDtoList(rates);
                                    model.addAttribute("rates", ratesDto);
                                    log.info("Загружено {} курсов валют для отображения", ratesDto.size());

                                    return renderPage(model, "page/dashboard", "Главная страница",
                                            "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                                })
                                .onErrorResume(e -> {
                                    log.warn("Не удалось загрузить курсы валют: {}", e.getMessage());
                                    model.addAttribute("rates", List.of());
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
    }
}
