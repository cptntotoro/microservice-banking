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
 * Минимальный мок контроллер для проверки работоспособности главной страницы
 */
@Controller
public class DashboardMockController extends BaseController {

    @GetMapping("/dashboard-mock")
    public Mono<String> showDashboardMock(Model model) {
        // Мок-данные для пользователя
        UserDashboardDto mockUser = UserDashboardDto.builder()
                .uuid(UUID.randomUUID())
                .username("mockuser")
                .firstName("Иван")
                .lastName("Иванов")
                .email("ivan.ivanov@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .accounts(List.of(
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("RUB")
                                .balance(new BigDecimal("10000.00"))
                                .build(),
                        AccountDashboardDto.builder()
                                .id(UUID.randomUUID())
                                .currencyCode("USD")
                                .balance(new BigDecimal("500.00"))
                                .build()
                ))
                .build();

        model.addAttribute("user", mockUser);
        model.addAttribute("userAccounts", mockUser.getAccounts());

        // Инициализируем формы для переводов (пустые, как в оригинале)
        model.addAttribute("ownTransferRequest", new OwnTransferRequestDto());
        model.addAttribute("otherTransferRequest", new OtherTransferRequestDto());

        // Мок-данные для курсов валют
        List<ExchangeRateDto> mockRates = List.of(
                ExchangeRateDto.builder()
                        .code("USD")
                        .buyValue(new BigDecimal("90.00"))
                        .sellValue(new BigDecimal("92.00"))
                        .build(),
                ExchangeRateDto.builder()
                        .code("EUR")
                        .buyValue(new BigDecimal("100.00"))
                        .sellValue(new BigDecimal("102.00"))
                        .build(),
                ExchangeRateDto.builder()
                        .code("CNY")
                        .buyValue(new BigDecimal("12.00"))
                        .sellValue(new BigDecimal("13.00"))
                        .build()
        );
        model.addAttribute("rates", mockRates);

        // Пример success/error для тестирования (закомментировано, чтобы не отображать по умолчанию)
        // model.addAttribute("success", "Операция успешна");
        // model.addAttribute("error", "Ошибка операции");

        return renderPage(model, "page/dashboard", "Главная страница (Mock)",
                "Главная страница приложения BankingApp (Mock версия для проверки разметки)", "page/dashboard", "dashboard");
    }
}