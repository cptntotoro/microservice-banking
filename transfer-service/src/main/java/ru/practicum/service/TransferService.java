package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

public interface TransferService {
    Mono<TransferResponse> transferBetweenOwnAccounts(TransferRequest request);

    Mono<TransferResponse> transferToOtherAccount(TransferRequest request);
}
