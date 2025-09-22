package ru.practicum.controller.profile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.user.AuthServiceClient;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.auth.ChangePasswordRequestDto;
import ru.practicum.dto.auth.UserProfileResponseDto;
import ru.practicum.dto.user.EditUserProfileDto;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController extends BaseController {

    private final AuthServiceClient authServiceClient;

    @PostMapping("/update")
    public Mono<String> updateProfile(@ModelAttribute @Valid EditUserProfileDto updateRequest,
                                      BindingResult bindingResult,
                                      ServerWebExchange exchange,
                                      Model model) {

        return getAuthToken(exchange).flatMap(token -> {
            if (token.isEmpty()) {
                return Mono.just("redirect:/login");
            }

            if (bindingResult.hasErrors()) {
                return authServiceClient.getProfile("Bearer " + token)
                        .flatMap(profile -> {
                            model.addAttribute("userProfile", profile);
                            model.addAttribute("error", "Проверьте правильность введенных данных");
                            return renderPage(model, "auth/profile",
                                    "Профиль", "Управление профилем",
                                    "auth/profile", null);
                        });
            }

            return authServiceClient.updateProfile(updateRequest, "Bearer " + token)
                    .flatMap(updatedProfile -> {
                        // Обновляем профиль в сессии
                        exchange.getSession().subscribe(session -> {
                            session.getAttributes().put("user_profile", updatedProfile);
                        });

                        model.addAttribute("success", "Профиль успешно обновлен");
                        model.addAttribute("userProfile", updatedProfile);
                        return renderPage(model, "auth/profile",
                                "Профиль", "Управление профилем",
                                "auth/profile", null);
                    })
                    .onErrorResume(e -> {
                        model.addAttribute("error", "Ошибка при обновлении профиля");
                        return authServiceClient.getProfile("Bearer " + token)
                                .flatMap(profile -> {
                                    model.addAttribute("userProfile", profile);
                                    return renderPage(model, "auth/profile",
                                            "Профиль", "Управление профилем",
                                            "auth/profile", null);
                                });
                    });
        });
    }

    @PostMapping("/change-password")
    public Mono<String> changePassword(@ModelAttribute @Valid ChangePasswordRequestDto changeRequest,
                                       BindingResult bindingResult,
                                       ServerWebExchange exchange,
                                       Model model) {

        return getAuthToken(exchange).flatMap(token -> {
            if (token.isEmpty()) {
                return Mono.just("redirect:/login");
            }

            if (bindingResult.hasErrors()) {
                return authServiceClient.getProfile("Bearer " + token)
                        .flatMap(profile -> {
                            model.addAttribute("userProfile", profile);
                            model.addAttribute("passwordError", "Проверьте правильность введенных данных");
                            return renderPage(model, "auth/profile",
                                    "Профиль", "Управление профилем",
                                    "auth/profile", null);
                        });
            }

            if (!changeRequest.getNewPassword().equals(changeRequest.getConfirmPassword())) {
                return authServiceClient.getProfile("Bearer " + token)
                        .flatMap(profile -> {
                            model.addAttribute("userProfile", profile);
                            model.addAttribute("passwordError", "Пароли не совпадают");
                            return renderPage(model, "auth/profile",
                                    "Профиль", "Управление профилем",
                                    "auth/profile", null);
                        });
            }

            return authServiceClient.changePassword(changeRequest, "Bearer " + token)
                    .flatMap(response -> {
                        model.addAttribute("userProfile", (UserProfileResponseDto) exchange.getSession()
                                .map(session -> session.getAttributes().get("user_profile"))
                                .block());
                        model.addAttribute("passwordSuccess", "Пароль успешно изменен");
                        return renderPage(model, "auth/profile",
                                "Профиль", "Управление профилем",
                                "auth/profile", null);
                    })
                    .onErrorResume(e -> {
                        model.addAttribute("passwordError", "Неверный текущий пароль");
                        return authServiceClient.getProfile("Bearer " + token)
                                .flatMap(profile -> {
                                    model.addAttribute("userProfile", profile);
                                    return renderPage(model, "auth/profile",
                                            "Профиль", "Управление профилем",
                                            "auth/profile", null);
                                });
                    });
        });
    }

    private Mono<String> getAuthToken(ServerWebExchange exchange) {
        return exchange.getSession()
                .map(session -> (String) session.getAttributes().get("access_token"))
                .defaultIfEmpty("");
    }
}
