package ru.practicum.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.auth.LoginRequestDto;
import ru.practicum.dto.auth.SignUpRequestDto;
import ru.practicum.service.auth.AuthService;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AuthController extends BaseController {
    /**
     * Сервис авторизации
     */
    private final AuthService authService;

    @GetMapping("/login")
    public Mono<String> login(ServerWebExchange exchange, Model model,
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "success", required = false) String success,
                              @RequestParam(value = "logout", required = false) String logout) {
        if (error != null) {
            model.addAttribute("error", "Неверные учетные данные");
        }
        if (success != null) {
            model.addAttribute("success", "Регистрация прошла успешно! Теперь вы можете войти.");
        }
        if (logout != null) {
            model.addAttribute("success", "Вы успешно вышли из системы.");
        }

        model.addAttribute("loginRequest", new LoginRequestDto());
        return renderPage(model, "auth/login",
                "Войти", "Залогиниться в приложение",
                "auth/login", null);
    }

    /**
     * Обработка логина и сохранение токена в сессии
     */
    @PostMapping("/login")
    public Mono<String> performLogin(@ModelAttribute @Valid LoginRequestDto loginRequest,
                                     ServerWebExchange exchange,
                                     Model model) {
        return authService.login(loginRequest)
                .flatMap(tokenResponse -> exchange.getSession().flatMap(session -> {
                    session.getAttributes().put("access_token", tokenResponse.getAccessToken());
//                    session.getAttributes().put("refresh_token", tokenResponse.getRefreshToken());
                    return Mono.just("redirect:dashboard");
                }))
                .onErrorResume(e -> {
                    model.addAttribute("error", "Неверные учетные данные: " + e.getMessage());
                    model.addAttribute("loginRequest", loginRequest);
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

        return authService.createUser(signupRequest)
                .flatMap(signupResponse -> {
                    // После успешной регистрации перенаправляем на страницу логина
                    return encodeSuccessRedirect("login", "Регистрация успешна! Теперь вы можете войти в систему.");
                })
                .onErrorResume(e -> {
                    model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
                    model.addAttribute("signupRequest", signupRequest);
                    return renderPage(model, "auth/signup",
                            "Регистрация", "Регистрация в приложении",
                            "auth/signup", "auth/signup");
                });
    }

    @GetMapping("/logout")
    public Mono<String> performLogout(ServerWebExchange exchange) {
        return exchange.getSession().doOnNext(authService::logout)
                .then(encodeSuccessRedirect("login", "Вы разлогинились!"));
    }
}
