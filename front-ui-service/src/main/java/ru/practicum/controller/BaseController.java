package ru.practicum.controller;

import org.springframework.ui.Model;
import reactor.core.publisher.Mono;

public abstract class BaseController {

//    /**
//     * Отрисовать страницу
//     *
//     * @param model Модель
//     * @param page Путь до HTML файла страницы из корня /templates/
//     * @param title Название страницы (SEO)
//     * @param description Описание страницы (SEO)
//     * @return Финальная страница
//     */
//    protected Mono<String> renderPage(Model model, String page, String title, String description) {
//        return renderPage(model, page, title, description, page, page);
//    }

    /**
     * Отрисовать страницу
     *
     * @param model Модель
     * @param page Путь до HTML файла страницы из корня /templates/
     * @param title Название страницы (SEO)
     * @param description Описание страницы (SEO)
     * @param styles Путь до CSS файла формата @{'/styles/' + ${styles} + '.css'}
     * @param scripts Путь до JavaScript файла страницы из корня /scripts/
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
}