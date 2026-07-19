package pl.dudios.shop.common.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * API sklepu nie ma prefiksu /api, więc część tras Angulara pokrywa się
 * z endpointami REST (GET /orders, /admin/orders, ...). Przy twardym
 * odświeżeniu przeglądarka wysyła Accept: text/html — ten kontroler wygrywa
 * wtedy negocjację treści i oddaje SPA; XHR-y z Accept: application/json
 * trafiają normalnie do API.
 */
@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/orders",
            "/product/*",
            "/admin",
            "/admin/products",
            "/admin/product/*",
            "/admin/categories",
            "/admin/orders",
            "/admin/order/*",
    }, produces = MediaType.TEXT_HTML_VALUE)
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }

}
