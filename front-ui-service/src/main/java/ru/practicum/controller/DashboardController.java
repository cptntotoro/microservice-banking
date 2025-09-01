package ru.practicum.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public Mono<String> dashboard(ServerWebExchange exchange, Model model) {
        model.addAttribute("page", "dashboard");
        model.addAttribute("styles", "dashboard");
        model.addAttribute("script", "dashboard");
        return Mono.just("layout");
    }
}
