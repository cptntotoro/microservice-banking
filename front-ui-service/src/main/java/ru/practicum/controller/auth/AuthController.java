package ru.practicum.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;
import ru.practicum.dto.auth.SignupFormDto;

@Controller
@RequiredArgsConstructor
public class AuthController extends BaseController {

    @GetMapping("/login")
    public Mono<String> login(ServerWebExchange exchange, Model model) {
        return renderPage(model, "auth/login",
                "Войти", "Залогиниться в приложение",
                "auth/login", null);
    }

    @GetMapping("/signup")
    public Mono<String> signup(Model model) {
        model.addAttribute("signup", new SignupFormDto());
        return renderPage(model, "auth/signup",
                "Регистрация", "Регистрация в приложении",
                "auth/signup", null);
    }

    @GetMapping("/logout")
    public Mono<String> logout(Model model) {
        return renderPage(model, "auth/logout",
                "Выход", "auth/logout",
                null, null);
    }

//    @PostMapping("/signup")
//    public Mono<String> signup(@Valid @ModelAttribute("signup") SignupFormDto signupFormDto, Model model) {
////        return userService.register(userMapper.userAuthDtoToUser(userAuthDto))
////                .then(Mono.just("redirect:/login"))
////                .onErrorResume(UserAlreadyExistsException.class, e -> {
////                    model.addAttribute("userExists", true);
////                    return Mono.just("auth/sign-up");
////                });
//    }
}
