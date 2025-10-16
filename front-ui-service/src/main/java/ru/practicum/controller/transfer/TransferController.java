package ru.practicum.controller.transfer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.service.transfer.TransferService;

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
                .then(encodeSuccessRedirect("dashboard", "Перевод успешно выполнен"))
                .onErrorResume(e -> encodeErrorRedirect("dashboard", "Произошла непредвиденная ошибка: " + e.getMessage())));
    }

    @PostMapping("/other")
    public Mono<String> handleOtherTransfer(@ModelAttribute @Valid OtherTransferRequestDto requestDto, ServerWebExchange exchange, Model model) {
        log.info("Обработка запроса перевода между счетами: getFromAccountId={}, getToCurrency={}, getRecipientEmail={}, getAmount={}",
                requestDto.getFromAccountId(), requestDto.getToCurrency(), requestDto.getRecipientEmail(), requestDto.getAmount());

        return withAuthenticatedUser(exchange, userId -> transferService.performOtherTransfer(requestDto, userId)
                .then(encodeSuccessRedirect("dashboard", "Перевод успешно выполнен"))
                .onErrorResume(e -> encodeErrorRedirect("dashboard", "Произошла непредвиденная ошибка: " + e.getMessage())));
    }
}