package ru.practicum.controller.transfer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/transfer")
@Slf4j
@RequiredArgsConstructor
public class TransferController extends BaseController {
    /**
     * Сервис перевода средств
     */
    private final TransferService transferService;

    @PostMapping("/own")
    public Mono<String> handleOwnTransfer(@ModelAttribute @Valid OwnTransferRequestDto requestDto, ServerWebExchange exchange, Model model) {
        log.info("Обработка запроса перевода между своими счетами: getFromAccountId={}, ToAccountId={}, getAmount={}",
                requestDto.getFromAccountId(), requestDto.getToAccountId(), requestDto.getAmount());

        return withAuthenticatedUser(exchange, userId -> transferService.performOwnTransfer(requestDto, userId)
                .flatMap(result -> encodeSuccessRedirect("dashboard", "Перевод успешно выполнен"))
                .onErrorResume(e -> encodeErrorRedirect("dashboard", "Произошла непредвиденная ошибка: " + e.getMessage())));
    }

    @PostMapping("/other")
    public Mono<String> handleOtherTransfer(@ModelAttribute @Valid OtherTransferRequestDto requestDto, ServerWebExchange exchange, Model model) {
        log.info("Обработка запроса перевода между счетами: getFromAccountId={}, getToCurrency={}, getRecipientEmail={}, getAmount={}",
                requestDto.getFromAccountId(), requestDto.getToCurrency(), requestDto.getRecipientEmail(), requestDto.getAmount());

        return withAuthenticatedUser(exchange, userId -> transferService.performOtherTransfer(mapToOtherTransferModel(userId, requestDto))
                .flatMap(result -> encodeSuccessRedirect("dashboard", "Перевод успешно выполнен"))
                .onErrorResume(e -> encodeErrorRedirect("dashboard", "Произошла непредвиденная ошибка: " + e.getMessage())));
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