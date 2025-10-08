package ru.practicum.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * Адаптер реактивного репозитория в блокирующий для Spring Security OAuth2
 */
@Slf4j
public class ReactiveToBlockingClientRepositoryAdapter implements RegisteredClientRepository {

    private final ReactiveRegisteredClientRepository reactiveRepository;

    public ReactiveToBlockingClientRepositoryAdapter(ReactiveRegisteredClientRepository reactiveRepository) {
        this.reactiveRepository = reactiveRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        log.info("Saving registered client {}", registeredClient.toString());
        reactiveRepository.save(registeredClient).block();
    }

    @Override
    public RegisteredClient findById(String id) {
        log.info("Finding registered client {}", id);
        return reactiveRepository.findById(id).block();
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        log.info("Finding registered client {}", clientId);
        return reactiveRepository.findByClientId(clientId).block();
    }
}