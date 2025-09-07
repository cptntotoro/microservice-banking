//package ru.practicum.controller.cash;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//import ru.practicum.client.CashServiceClient;
//import ru.practicum.client.ExchangeServiceClient;
//import ru.practicum.controller.BaseController;
//import ru.practicum.dto.cash.CashOperationRequestDto;
//
//@Controller
//@RequiredArgsConstructor
//@RequestMapping("/cash")
//public class CashController extends BaseController {
//
//    private final CashServiceClient cashServiceClient;
//    private final ExchangeServiceClient exchangeServiceClient;
//
//    @PostMapping("/deposit")
//    public Mono<String> deposit(@ModelAttribute CashOperationRequestDto request,
//                                ServerWebExchange exchange,
//                                Model model) {
//        return getAuthToken(exchange).flatMap(token -> {
//            if (token.isEmpty()) {
//                return Mono.just("redirect:/login");
//            }
//
//            return cashServiceClient.deposit(request, "Bearer " + token)
//                    .then(Mono.just("redirect:/"))
//                    .onErrorResume(e -> {
//                        model.addAttribute("error", "Ошибка при пополнении: " + e.getMessage());
//                        return home(exchange, model);
//                    });
//        });
//    }
//
//    @PostMapping("/withdraw")
//    public Mono<String> withdraw(@ModelAttribute CashOperationRequestDto request,
//                                 ServerWebExchange exchange,
//                                 Model model) {
//        return getAuthToken(exchange).flatMap(token -> {
//            if (token.isEmpty()) {
//                return Mono.just("redirect:/login");
//            }
//
//            return cashServiceClient.withdraw(request, "Bearer " + token)
//                    .then(Mono.just("redirect:/"))
//                    .onErrorResume(e -> {
//                        model.addAttribute("error", "Ошибка при снятии: " + e.getMessage());
//                        return home(exchange, model);
//                    });
//        });
//    }
//
//    private Mono<String> getAuthToken(ServerWebExchange exchange) {
//        return exchange.getSession()
//                .map(session -> (String) session.getAttributes().get("access_token"))
//                .defaultIfEmpty("");
//    }
//}
