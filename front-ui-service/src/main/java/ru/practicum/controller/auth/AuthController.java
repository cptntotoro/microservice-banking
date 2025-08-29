package ru.practicum.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.dto.auth.SignupFormDto;

@Controller
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/login")
    public Mono<String> login(ServerWebExchange exchange, Model model) {
        return Mono.just("auth/login");
    }

    @GetMapping("/signup")
    public Mono<String> signupForm(Model model) {
        return Mono.just(model.addAttribute("signup", new SignupFormDto()))
                .thenReturn("auth/signup");
    }

    @PostMapping("/signup")
    public Mono<String> signup(@Valid @ModelAttribute("signup") SignupFormDto signupFormDto, Model model) {
//        return userService.register(userMapper.userAuthDtoToUser(userAuthDto))
//                .then(Mono.just("redirect:/login"))
//                .onErrorResume(UserAlreadyExistsException.class, e -> {
//                    model.addAttribute("userExists", true);
//                    return Mono.just("auth/sign-up");
//                });
    }
}
