package ru.practicum.controller;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.config.JwtConfig;
import ru.practicum.repository.ReactiveRegisteredClientRepository;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class TokenController {

    @Autowired
    private ReactiveRegisteredClientRepository clientRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @PostMapping("/oauth2/token")
    public Mono<Map<String, Object>> token(ServerWebExchange exchange) {
        return exchange.getFormData().flatMap(formData -> {
            String grantType = formData.getFirst("grant_type");
            String scope = formData.getFirst("scope");

            log.warn("token grant type: " + grantType);
            log.warn("scope: " + scope);

            if (grantType == null || !grantType.equals("client_credentials")) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported grant_type"));
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header"));
            }

            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded);
            String[] parts = credentials.split(":", 2);
            if (parts.length != 2) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Basic Auth format"));
            }

            String clientId = parts[0];
            String clientSecret = parts[1];

            log.warn("client id: " + clientId);
            log.warn("client secret: " + clientSecret);

            return clientRepository.findByClientId(clientId)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid client credentials")))
                    .flatMap(client -> {
                        log.warn("client: " + client.toString());
                        log.warn("clientSecret: " + client.getClientSecret());
                        if (!passwordEncoder.matches(clientSecret, client.getClientSecret())) {
                            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid client credentials"));
                        }

                        // Проверяем, что запрошенные scopes разрешены для клиента
                        String[] requestedScopes = scope != null ? scope.split(" ") : new String[]{};
                        for (String requestedScope : requestedScopes) {
                            if (!client.getScopes().contains(requestedScope)) {
                                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Scope " + requestedScope + " not allowed for client"));
                            }
                        }

                        return generateJwt(client, scope).map(token -> {
                            Map<String, Object> response = new HashMap<>();
                            response.put("access_token", token);
                            response.put("token_type", "Bearer");
                            response.put("expires_in", 3600);
                            response.put("scope", scope != null ? scope : client.getScopes().stream().collect(Collectors.joining(" ")));
                            return response;
                        });
                    });
        });
    }

    private Mono<String> generateJwt(RegisteredClient client, String scope) {
        return Mono.fromCallable(() -> {
            JWKSelector jwkSelector = new JWKSelector(new JWKMatcher.Builder()
                    .keyType(KeyType.RSA)
                    .build());
            JWK jwk = jwtConfig.jwkSource().get(jwkSelector, null).get(0);
            RSASSASigner signer = new RSASSASigner(jwk.toRSAKey().toPrivateKey());

            log.warn("Generating JWT for clientId: {}, scope: {}", client.getClientId(), scope);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(client.getClientId())
                    .issuer(issuerUri)
                    .expirationTime(new Date(System.currentTimeMillis() + 3600 * 1000))
                    .claim("scope", scope != null ? scope : client.getScopes().stream().collect(Collectors.joining(" ")))
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        }).onErrorMap(e -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate JWT", e));
    }
}