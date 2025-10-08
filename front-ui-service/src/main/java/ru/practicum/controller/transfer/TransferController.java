package ru.practicum.controller.transfer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.auth.dto.LoginResponseClientDto;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;
import ru.practicum.model.transfer.TransferResult;
import ru.practicum.model.transfer.TransferStatus;
import ru.practicum.service.transfer.TransferService;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class TransferController extends BaseController {
    /**
     * Сервис перевода средств
     */
    private final TransferService transferService;

    @PostMapping("/transfer/own")
    public Mono<String> handleOwnTransfer(ServerWebExchange exchange,
                                          @Valid @ModelAttribute("ownTransferRequest") OwnTransferRequestDto request,
                                          BindingResult bindingResult,
                                          Model model) {

        return exchange.getSession().flatMap(session -> {
            LoginResponseClientDto userData = (LoginResponseClientDto) session.getAttributes().get("userData");

            if (userData == null) {
                return Mono.just("redirect:/login");
            }

            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Проверьте правильность введенных данных");
                return loadDashboardData(userData, model, exchange);
            }

            // Маппинг DTO в модель для сервиса
            OwnTransfer transferModel = mapToOwnTransferModel(userData.getUserId(), request);

            return transferService.performOwnTransfer(transferModel)
                    .flatMap(result -> handleTransferResult(result, userData, model))
                    .onErrorResume(e -> {
                        model.addAttribute("error", "Произошла непредвиденная ошибка: " + e.getMessage());
                        return loadDashboardData(userData, model, exchange);
                    });
        });
    }

    @PostMapping("/transfer/other")
    public Mono<String> handleOtherTransfer(ServerWebExchange exchange,
                                            @Valid @ModelAttribute("otherTransferRequest") OtherTransferRequestDto request,
                                            BindingResult bindingResult,
                                            Model model) {

        return exchange.getSession().flatMap(session -> {
            LoginResponseClientDto userData = (LoginResponseClientDto) session.getAttributes().get("userData");

            if (userData == null) {
                return Mono.just("redirect:/login");
            }

            if (bindingResult.hasErrors()) {
                model.addAttribute("error", "Проверьте правильность введенных данных");
                return loadDashboardData(userData, model, exchange);
            }

            // Маппинг DTO в модель для сервиса
            OtherTransfer transferModel = mapToOtherTransferModel(userData.getUserId(), request);

            return transferService.performOtherTransfer(transferModel)
                    .flatMap(result -> handleTransferResult(result, userData, model))
                    .onErrorResume(e -> {
                        model.addAttribute("error", "Произошла непредвиденная ошибка: " + e.getMessage());
                        return loadDashboardData(userData, model, exchange);
                    });
        });
    }

    /**
     * Обработка результата перевода
     */
    private Mono<String> handleTransferResult(TransferResult result, LoginResponseClientDto userData, Model model) {
        if (TransferStatus.SUCCESS.equals(result.getStatus())) {
            model.addAttribute("success", "Перевод успешно выполнен!");
            model.addAttribute("lastTransferResult", result);
        } else {
            model.addAttribute("error", result.getMessage());
            model.addAttribute("lastTransferResult", result);
        }

        model.addAttribute("userData", userData);
        return renderPage(model, "page/dashboard", "Главная страница",
                "Главная страница приложения BankingApp", "page/dashboard", "dashboard");
    }

    /**
     * Загрузка данных для dashboard
     */
    private Mono<String> loadDashboardData(LoginResponseClientDto userData, Model model, ServerWebExchange exchange) {
        model.addAttribute("userData", userData);
        model.addAttribute("ownTransferRequest", new OwnTransferRequestDto());
        model.addAttribute("otherTransferRequest", new OtherTransferRequestDto());

        // Обработка сообщений из query params
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
    }

    // Маппинг DTO в модели
    private OwnTransfer mapToOwnTransferModel(UUID userId, OwnTransferRequestDto request) {
        return OwnTransfer.builder()
                .userId(userId)
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .build();
    }

    private OtherTransfer mapToOtherTransferModel(UUID userId, OtherTransferRequestDto request) {
        return OtherTransfer.builder()
                .userId(userId)
                .fromAccountId(request.getFromAccountId())
                .toCurrency(request.getToCurrency())
                .recipientEmail(request.getRecipientEmail())
                .amount(request.getAmount())
                .build();
    }
}