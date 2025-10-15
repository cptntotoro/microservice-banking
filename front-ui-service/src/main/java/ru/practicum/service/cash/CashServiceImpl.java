package ru.practicum.service.cash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.dto.AccountRequestDto;
import ru.practicum.client.cash.CashServiceClient;
import ru.practicum.client.cash.dto.CashRequestClientDto;
import ru.practicum.dto.cash.DepositWithdrawCashRequestDto;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.cash.CashMapper;
import ru.practicum.model.cash.Cash;
import ru.practicum.service.account.AccountService;
import ru.practicum.service.exchange.ExchangeRateService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    private static final String DEPOSIT = "deposit";
    private static final String WITHDRAW = "withdraw";

    /**
     * Клиент для обращений к сервису обналичивания денег (cash-service)
     */
    private final CashServiceClient cashServiceClient;

    /**
     * Маппер операций с наличными
     */
    private final CashMapper cashMapper;

    /**
     * Сервис курсов обмена валют
     */
    private final ExchangeRateService exchangeRateService;
    private final AccountService accountService;


    @Override
    public Mono<Cash> cashOperation(UUID userId, DepositWithdrawCashRequestDto requestDto) {
        return validateCurrency(requestDto.getCurrency())
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Недопустимая валюта для {}: {}", requestDto.getOperation(), requestDto.getCurrency());
                        return Mono.error(new ValidationException("Недопустимая валюта: " + requestDto.getCurrency()));
                    }
                    return accountService.getAccount(AccountRequestDto.builder().userId(userId).currencyCode(requestDto.getCurrency()).build())
                            .flatMap(account -> {
                                CashRequestClientDto cashRequestClientDto = CashRequestClientDto.builder()
                                        .accountId(account.getId())
                                        .userId(userId)
                                        .amount(requestDto.getAmount())
                                        .currency(requestDto.getCurrency())
                                        .isDeposit(requestDto.getOperation().equals(DEPOSIT))
                                        .build();

                                log.info("Обработка {} для счета {}: сумма {}", requestDto.getOperation(), cashRequestClientDto.getAccountId(), cashRequestClientDto.getAmount());

                                return cashServiceClient.cashOperation(cashRequestClientDto)
                                        .map(cashMapper::cashResponseClientDtoToCash);
                            });
                });
    }

    // Вспомогательный метод для валидации валюты
    private Mono<Boolean> validateCurrency(String currency) {
        if (currency == null) {
            return Mono.just(false);
        }
        return exchangeRateService.getAvailableCurrencies()
                .collectList()
                .map(currencies -> currencies.contains(currency))
                .defaultIfEmpty(false)
                .doOnNext(valid -> log.debug("Валидация валюты {}: {}", currency, valid));
    }

}