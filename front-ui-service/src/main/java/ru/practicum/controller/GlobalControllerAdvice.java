//package ru.practicum.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import reactor.core.publisher.Mono;
//
//@ControllerAdvice(basePackages = {
//        "ru.practicum.controller.cart",
//        "ru.practicum.controller.order",
//        "ru.practicum.controller.payment",
//        "ru.practicum.controller.product"
//})
//@RequiredArgsConstructor
//public class GlobalControllerAdvice {
//
//    @ModelAttribute
//    public Mono<Void> addCommonAttributes(@AuthenticationPrincipal User user,
//                                          Model model) {
//        boolean isAuthenticated = user != null;
//
//        model.addAttribute("isAuthenticated", isAuthenticated);
//
//        if (!isAuthenticated) {
//            return Mono.empty();
//        }
//
//        return cartService.get(user.getUuid())
//                .map(cartMapper::cartToCartDto)
//                .doOnNext(cartDto -> model.addAttribute("cart", cartDto))
//                .then();
//    }
//}
