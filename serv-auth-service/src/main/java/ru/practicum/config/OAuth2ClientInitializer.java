package ru.practicum.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import reactor.core.publisher.Mono;
import ru.practicum.repository.ReactiveRegisteredClientRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class OAuth2ClientInitializer {

    @Bean
    public ApplicationRunner initializeClients(ReactiveRegisteredClientRepository clientRepository) {
        return args -> {
            clientRepository.findByClientId("front-ui-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient frontUiService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("front-ui-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("account-service.read")
                                .scope("account-service.write")
                                .scope("user-auth-service.read")
                                .scope("user-auth-service.write")
                                .scope("cash-service.read")
                                .scope("cash-service.write")
                                .scope("exchange-service.read")
                                .scope("exchange-service.write")
                                .scope("transfer-service.read")
                                .scope("transfer-service.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(frontUiService).thenReturn(frontUiService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("account-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient accountService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("account-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(accountService).thenReturn(accountService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("user-auth-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient userAuthService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("user-auth-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("account-service.read")
                                .scope("account-service.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(userAuthService).thenReturn(userAuthService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("blocker-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient blockerService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("blocker-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(blockerService).thenReturn(blockerService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("cash-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient cashService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("cash-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("account-service.read")
                                .scope("account-service.write")
                                .scope("blocker-service.read")
                                .scope("blocker-service.write")
                                .scope("notification-service.read")
                                .scope("notification-service.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(cashService).thenReturn(cashService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("exchange-generator-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient exchangeGeneratorService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("exchange-generator-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("exchange-service.read")
                                .scope("exchange-service.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(exchangeGeneratorService).thenReturn(exchangeGeneratorService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("exchange-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient exchangeService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("exchange-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(exchangeService).thenReturn(exchangeService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("notification-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient notificationService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("notification-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(notificationService).thenReturn(notificationService);
                    }))
                    .subscribe();

            clientRepository.findByClientId("transfer-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient transferService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("transfer-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("account-service.read")
                                .scope("account-service.write")
                                .scope("blocker-service.read")
                                .scope("blocker-service.write")
                                .scope("exchange-service.read")
                                .scope("exchange-service.write")
                                .scope("notification-service.read")
                                .scope("notification-service.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(transferService).thenReturn(transferService);
                    }))
                    .subscribe();
        };
    }
}