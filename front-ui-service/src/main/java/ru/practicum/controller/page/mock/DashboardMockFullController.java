package ru.practicum.controller.page.mock;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.account.AccountDashboardDto;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.dto.user.UserDashboardDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Полный мок контроллер для проверки работоспособности главной страницы
 */
@Controller
public class DashboardMockFullController extends BaseController {

    @GetMapping("/dashboard-mock-full")
    public Mono<String> showDashboardMockFull(Model model) {
        UserDashboardDto mockUser = UserDashboardDto.builder()
                .uuid(UUID.randomUUID())
                .username("fullmockuser")
                .firstName("Александр")
                .lastName("Петров")
                .email("alex.petrov@example.com")
                .birthDate(LocalDate.of(1985, 5, 15))
                .accounts(List.of(
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("RUB")
                                .balance(new BigDecimal("150000.50"))
                                .build(),
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("USD")
                                .balance(new BigDecimal("1000.75"))
                                .build(),
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("EUR")
                                .balance(new BigDecimal("800.20"))
                                .build(),
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("CNY")
                                .balance(new BigDecimal("5000.00"))
                                .build()
                ))
                .build();

        model.addAttribute("user", mockUser);
        model.addAttribute("userAccounts", mockUser.getAccounts());

        // Инициализируем формы для переводов с предзаполненными данными для тестирования
        OwnTransferRequestDto ownTransfer = OwnTransferRequestDto.builder()
                .userId(mockUser.getUuid())
                .fromAccountId(mockUser.getAccounts().get(0).getId())
                .toAccountId(mockUser.getAccounts().get(1).getId())
                .amount(new BigDecimal("100.00"))
                .build();
        model.addAttribute("ownTransferRequest", ownTransfer);

        OtherTransferRequestDto otherTransfer = OtherTransferRequestDto.builder()
                .fromUserId(mockUser.getUuid())
                .fromAccountId(mockUser.getAccounts().get(0).getId())
                .toCurrency("USD")
                .recipientEmail("recipient@example.com")
                .amount(new BigDecimal("200.00"))
                .build();
        model.addAttribute("otherTransferRequest", otherTransfer);

        // Мок-данные для всех поддерживаемых валют
        List<ExchangeRateDto> mockRates = List.of(
                ExchangeRateDto.builder()
                        .code("USD")
                        .buyValue(new BigDecimal("90.50"))
                        .sellValue(new BigDecimal("92.50"))
                        .build(),
                ExchangeRateDto.builder()
                        .code("EUR")
                        .buyValue(new BigDecimal("100.75"))
                        .sellValue(new BigDecimal("102.75"))
                        .build(),
                ExchangeRateDto.builder()
                        .code("CNY")
                        .buyValue(new BigDecimal("12.20"))
                        .sellValue(new BigDecimal("13.20"))
                        .build()
        );
        model.addAttribute("rates", mockRates);

        // Добавляем сообщения об успехе и ошибке для тестирования отображения
        model.addAttribute("success", "Перевод успешно выполнен!");
        model.addAttribute("error", "Ошибка: недостаточно средств на счете");

        return renderPage(model, "page/dashboard", "Главная страница (Full Mock)",
                "Полная версия главной страницы для проверки всех элементов dashboard.html", "page/dashboard", "dashboard");
    }
}