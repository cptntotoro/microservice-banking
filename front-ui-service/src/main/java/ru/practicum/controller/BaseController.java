package ru.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.service.auth.AuthService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public abstract class BaseController {

    @Autowired
    protected AuthService authService;

    /**
     * Validates session and retrieves user ID, executing the provided action if valid.
     *
     * @param exchange The ServerWebExchange containing session information
     * @param action   The action to perform with the user ID
     * @return Mono<String> representing the result of the action or a redirect on error
     */
    protected Mono<String> withAuthenticatedUser(ServerWebExchange exchange,
                                                 Function<UUID, Mono<String>> action) {
        return exchange.getSession().flatMap(session -> {
                    if (session.getAttributes().get("access_token") == null) {
                        log.warn("No access token found in session");
                        return encodeErrorRedirect("login", "Залогиньтесь");
                    }
                    return authService.getUserId((String) session.getAttributes().get("access_token"))
                            .flatMap(userId -> {
                                log.info("Юзер загружен: " + userId);
                                if (userId == null) {
                                    log.warn("User ID is null, session closed");
                                    return encodeErrorRedirect("login", "Залогиньтесь, сессия закрыта");
                                }
                                return action.apply(UUID.fromString(userId));
                            })
                            .onErrorResume(e -> {
                                log.error("Ошибка загрузки данных пользователя: {}", e.getMessage());
                                return encodeErrorRedirect("dashboard", "Ошибка загрузки данных пользователя: " + e.getMessage());
                            });
                })
                .onErrorResume(e -> {
                    log.error("Ошибка загрузки данных: {}", e.getMessage());
                    return encodeErrorRedirect("dashboard", "Ошибка загрузки данных: " + e.getMessage());
                });
    }

    /**
     * Отрисовать страницу
     *
     * @param model       Модель
     * @param page        Путь до HTML файла страницы из корня /templates/
     * @param title       Название страницы (SEO)
     * @param description Описание страницы (SEO)
     * @param styles      Путь до CSS файла формата @{'/styles/' + ${styles} + '.css'}
     * @param scripts     Путь до JavaScript файла страницы из корня /scripts/
     * @return Финальная страница
     */
    protected Mono<String> renderPage(Model model, String page, String title, String description,
                                      String styles, String scripts) {
        // TODO: вынести BankingApp в .properties
        model.addAttribute("title", title + " | BankingApp");
        model.addAttribute("description", description);
        model.addAttribute("page", page);
        model.addAttribute("styles", styles);
        model.addAttribute("script", scripts);
        return Mono.just("layout");
    }

    protected Mono<String> encodeSuccessRedirect(String path, String paramValue) {
        return encodeRedirect(path, "success", paramValue);
    }

    protected Mono<String> encodeErrorRedirect(String path, String paramValue) {
        return encodeRedirect(path, "error", paramValue);
    }

    protected Mono<String> encodeRedirect(String path, String paramName, String paramValue) {
        return Mono.just("redirect:/" + path + "?" + paramName + "=" + encode(paramValue));
    }

    private String encode(String paramValue) {
        return URLEncoder.encode(paramValue, StandardCharsets.UTF_8);
    }
}