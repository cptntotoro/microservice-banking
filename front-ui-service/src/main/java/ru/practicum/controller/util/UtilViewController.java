package ru.practicum.controller.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseController;

@Controller
@RequiredArgsConstructor
public class UtilViewController extends BaseController implements ErrorController {

    @GetMapping("/notfound")
    public Mono<String> notFound(Model model) {
        return renderPage(model, "util/not-found", "Страница не найдена",
                "Запрошенная страница не существует или была перемещена", null, null);
    }

    @GetMapping("/error")
    public Mono<String> error() {
        return Mono.just("util/error");
    }
}
