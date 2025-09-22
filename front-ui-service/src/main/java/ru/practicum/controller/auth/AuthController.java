package ru.practicum.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.client.account.user.AuthServiceClient;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;

@Controller
@RequiredArgsConstructor
public class AuthController extends BaseController {
    /**
     * Клиент обращения к сервису аутентификации
     */
    private final AuthServiceClient authServiceClient;

    /**
     * Клиент для обращений к сервису аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

    @GetMapping("/login")
    public Mono<String> login(ServerWebExchange exchange, Model model,
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "success", required = false) String success) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (success != null) {
            model.addAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти.");
        }

        model.addAttribute("loginRequest", new LoginRequestDto());
        return renderPage(model, "auth/login",
                "Войти", "Залогиниться в приложение",
                "auth/login", null);
    }

    @PostMapping("/login")
    public Mono<String> performLogin(@ModelAttribute @Valid LoginRequestDto loginRequest,
                                     ServerWebExchange exchange,
                                     Model model) {
        return accountServiceClient.login(loginRequest)
                .flatMap(loginResponse -> {
                    // Сохраняем userData в сессии
                    return exchange.getSession().flatMap(session -> {
                        session.getAttributes().put("userData", loginResponse);
                        return Mono.just("redirect:/dashboard");
                    });
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Неверные учетные данные");
                    return renderPage(model, "auth/login",
                            "Войти", "Залогиниться в приложение",
                            "auth/login", null);
                });
    }

    @GetMapping("/signup")
    public Mono<String> signup(Model model) {
        model.addAttribute("signupRequest", new SignUpRequestDto());
        return renderPage(model, "auth/signup",
                "Регистрация", "Регистрация в приложении",
                "auth/signup", "auth/signup");
    }

    @PostMapping("/signup")
    public Mono<String> performSignup(@ModelAttribute @Valid SignUpRequestDto signupRequest,
                                      ServerWebExchange exchange,
                                      Model model) {
        // Проверка возраста (старше 18 лет)
        if (signupRequest.getBirthDate().isAfter(java.time.LocalDate.now().minusYears(18))) {
            model.addAttribute("error", "Для регистрации необходимо быть старше 18 лет");
            model.addAttribute("signupRequest", signupRequest);
            return renderPage(model, "auth/signup",
                    "Регистрация", "Регистрация в приложении",
                    "auth/signup", "auth/signup");
        }

        return accountServiceClient.createAccount(signupRequest)
                .flatMap(signupResponse -> {
                    // После успешной регистрации перенаправляем на страницу логина
                    model.addAttribute("success", "Регистрация успешна! Теперь вы можете войти в систему.");
                    return Mono.just("redirect:/login?success");
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
                    model.addAttribute("signupRequest", signupRequest);
                    return renderPage(model, "auth/signup",
                            "Регистрация", "Регистрация в приложении",
                            "auth/signup", "auth/signup");
                });
    }

    @PostMapping("/logout")
    public Mono<String> performLogout(ServerWebExchange exchange, Model model) {
        return getAuthToken(exchange).flatMap(token -> {
            if (!token.isEmpty()) {
                // Вызываем logout на auth-service
                authServiceClient.logout("Bearer " + token).subscribe();
            }

//            // Очищаем сессию и контекст безопасности
//            exchange.getSession().subscribe(session -> {
//                session.getAttributes().clear();
//            });
//
//            return ReactiveSecurityContextHolder.clearContext()
//                    .then(Mono.just("redirect:/login?logout"));
//        });

            // Очищаем сессию
            exchange.getSession().subscribe(session -> {
                session.getAttributes().remove("access_token");
                session.getAttributes().remove("refresh_token");
                session.getAttributes().remove("user_profile");
            });

            return Mono.just("redirect:/login?logout");
        });
    }

    // Вспомогательный метод для получения токена из сессии
    private Mono<String> getAuthToken(ServerWebExchange exchange) {
        return exchange.getSession()
                .map(session -> (String) session.getAttributes().get("access_token"))
                .defaultIfEmpty("");
    }

//    @GetMapping("/profile")
//    public Mono<String> profile(ServerWebExchange exchange, Model model) {
//        return getAuthToken(exchange).flatMap(token -> {
//            if (token.isEmpty()) {
//                return Mono.just("redirect:/login");
//            }
//
//            return authServiceClient.getProfile("Bearer " + token)
//                    .flatMap(profile -> {
//                        model.addAttribute("userProfile", profile);
//                        return renderPage(model, "auth/profile",
//                                "Профиль", "Управление профилем",
//                                "auth/profile", null);
//                    })
//                    .onErrorResume(e -> {
//                        model.addAttribute("error", "Ошибка при загрузке профиля");
//                        return Mono.just("redirect:/");
//                    });
//        });
//    }
}
