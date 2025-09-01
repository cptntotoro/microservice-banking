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

    @GetMapping(value = "/login")
    public Mono<String> login(ServerWebExchange exchange, Model model) {
        model.addAttribute("page", "auth/login");
        model.addAttribute("styles", "auth/login");
        return Mono.just("layout");
    }

    @GetMapping("/signup")
    public Mono<String> signup(Model model) {
        model.addAttribute("page", "auth/signup");
        model.addAttribute("styles", "auth/signup");
        model.addAttribute("signup", new SignupFormDto());
        return Mono.just("layout");
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

    @GetMapping("/logout")
    public Mono<String> logout(Model model) {
        return Mono.just("auth/logout");
    }
}
