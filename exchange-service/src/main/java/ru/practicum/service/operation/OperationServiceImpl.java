package ru.practicum.service.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.OperationDao;
import ru.practicum.mapper.operation.OperationMapper;
import ru.practicum.model.operation.Operation;
import ru.practicum.repository.OperationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;

    @Override
    public Mono<Operation> saveOperation(Operation operation) {
        OperationDao operationDao = operationMapper.operationToOperationDao(operation);
        return operationRepository.save(operationDao)
                .map(operationMapper::operationDaotoOperation)
                .doOnSuccess(op -> log.info("Operation saved: {}", op));
    }

    @Override
    public Flux<Operation> getRecentOperations(int limit) {
        return operationRepository.findRecentOperations(limit)
                .map(operationMapper::operationDaotoOperation);
    }

    @Override
    public Flux<Operation> getOperationsByCurrencyPair(String fromCurrency, String toCurrency) {
        return operationRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .map(operationMapper::operationDaotoOperation);
    }

    @Override
    public Flux<String> getAllUsedCurrencies() {
        return operationRepository.findAllUsedCurrencies();
    }
}