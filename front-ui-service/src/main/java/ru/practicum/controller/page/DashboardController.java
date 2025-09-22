package ru.practicum.controller.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.client.account.user.LoginResponseClientDto;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DashboardController extends BaseController {
    /**
     * Клиент для обращений к сервису аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

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

                        return renderPage(model, "page/dashboard", "Главная страница",
                                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                    })
                    .onErrorResume(e -> {
                        model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
                        model.addAttribute("userData", userData);
                        return renderPage(model, "page/dashboard", "Главная страница",
                                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
                    });
        });
    }
}
