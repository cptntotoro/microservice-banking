package ru.practicum.controller.page;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;

@Controller
public class DashboardController extends BaseController {

    @GetMapping("/dashboard")
    public Mono<String> dashboard(ServerWebExchange exchange, Model model) {
        return renderPage(model, "page/dashboard", "Главная страница",
                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
    }
}
