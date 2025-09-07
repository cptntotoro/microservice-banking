package ru.practicum.controller.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.user.auth.LoginResponseDto;
import ru.practicum.controller.BaseController;

@Controller
@RequiredArgsConstructor
public class DashboardController extends BaseController {

    @GetMapping("/dashboard")
    public Mono<String> showDashboard(ServerWebExchange exchange, Model model) {
        return exchange.getSession().flatMap(session -> {
            LoginResponseDto userData = (LoginResponseDto) session.getAttributes().get("userData");

            if (userData == null) {
                return Mono.just("redirect:/login");
            }

            // Передаем всю модель userData
            model.addAttribute("userData", userData);

            return renderPage(model, "page/dashboard", "Главная страница",
                    "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
        });
    }
}
