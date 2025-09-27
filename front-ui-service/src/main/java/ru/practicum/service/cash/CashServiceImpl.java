package ru.practicum.service.cash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.cash.CashServiceClient;
import ru.practicum.mapper.cash.CashMapper;
import ru.practicum.model.cash.Cash;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    /**
     * Клиент для обращений к сервису обналичивания денег (cash-service)
     */
    private final CashServiceClient cashServiceClient;

    /**
     * Маппер операций с наличными
     */
    private final CashMapper cashMapper;

    @Override
    public Mono<Cash> deposit(Cash cash) {
        log.info("Processing deposit for account {}: amount {}", cash.getAccountId(), cash.getAmount());
        return cashServiceClient.deposit(cashMapper.cashToCashRequestClientDto(cash))
                .map(cashMapper::cashResponseClientDtoToCash);
    }

    @Override
    public Mono<Cash> withdraw(Cash cash) {
        log.info("Processing withdraw for account {}: amount {}", cash.getAccountId(), cash.getAmount());
        return cashServiceClient.withdraw(cashMapper.cashToCashRequestClientDto(cash))
                .map(cashMapper::cashResponseClientDtoToCash);
    }

}