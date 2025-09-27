package ru.practicum.controller.page;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.client.account.user.LoginResponseClientDto;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.service.exchange.ExchangeRateService;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController extends BaseController {
    /**
     * Клиент для обращений к сервису аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

    /**
     * Сервис курсов обмена валют
     */
    private final ExchangeRateService exchangeRateService;

    /**
     * Маппер операций с наличными
     */
    private final ExchangeRateMapper exchangeRateMapper;

    @GetMapping("/dashboard")
    public Mono<String> showDashboard(ServerWebExchange exchange, Model model) {
        return exchange.getSession().flatMap(session -> {
            LoginResponseClientDto userData = (LoginResponseClientDto) session.getAttributes().get("userData");

            if (userData == null) {
                return Mono.just("redirect:/login");
            }

            UUID userId = userData.getUserId();

            // Загружаем актуальные счета пользователя
            return accountServiceClient.getUserAccounts(userId)
                    .collectList()
                    .flatMap(accounts -> {
                        model.addAttribute("userData", userData);
                        model.addAttribute("userAccounts", accounts);

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
                                    model.addAttribute("rates", List.of()); // Пустой список при ошибке
                                    return renderPage(model, "page/dashboard", "Главная страница",
                                            "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Ошибка загрузки данных: {}", e.getMessage());
                        model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
                        model.addAttribute("userData", userData);
                        model.addAttribute("rates", List.of()); // Пустой список курсов

                        return renderPage(model, "page/dashboard", "Главная страница",
                                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                    });
        });
    }
}
