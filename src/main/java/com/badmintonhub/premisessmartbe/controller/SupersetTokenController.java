package com.badmintonhub.premisessmartbe.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/superset")
public class SupersetTokenController {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient web;

    public SupersetTokenController(
            WebClient.Builder builder,
            @Value("${superset.base-url:http://localhost:8088/api/v1}") String baseUrl
    ) {
        this.web = builder
                .baseUrl(baseUrl) // phải kèm /api/v1
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Value("${superset.username:admin}")
    private String supersetUser;

    @Value("${superset.password:admin}")
    private String supersetPass;

    @Value("${superset.referer:http://localhost:8088}")
    private String supersetReferer; // dùng cho header Referer khi POST guest_token

    @PostMapping("/token")
    public Mono<Map<String, Object>> issueToken(@RequestBody Map<String, Object> body) {
        String dashboardId = Objects.toString(body.get("dashboardId"), "").trim();
        if (dashboardId.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Thiếu dashboardId (UUID)"));
        }

        // 1) LOGIN → access_token
        return web.post().uri("/security/login")
                .body(BodyInserters.fromValue(Map.of(
                        "username", supersetUser,
                        "password", supersetPass,
                        "provider", "db",
                        "refresh", true
                )))
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .flatMap(login -> {
                    String access = extractAccessToken(login.get("access_token"));
                    if (access == null || access.isBlank()) {
                        return Mono.error(new IllegalStateException("Không lấy được access_token: " + login));
                    }

                    // ... giữ nguyên phần trên

                    return web.get().uri("/security/csrf_token/")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                            .retrieve()
                            .bodyToMono(MAP_TYPE)
                            .flatMap(csrfResp -> {
                                String csrf = extractCsrfToken(csrfResp);
                                if (csrf == null || csrf.isBlank()) {
                                    return Mono.error(new IllegalStateException("Không lấy được CSRF token: " + csrfResp));
                                }

                                Map<String, Object> payload = Map.of(
                                        "resources", List.of(Map.of("type", "dashboard", "id", dashboardId)),
                                        "rls", List.of(),
                                        "user", Map.of("username", "guest", "first_name", "Guest", "last_name", "Viewer")
                                );

                                return web.post().uri("/security/guest_token")
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + access)
                                        .header("X-CSRFToken", csrf)
                                        .header("Referer", supersetReferer)           // ví dụ http://localhost:8088
                                        .cookie("csrf_access_token", csrf)            // ✅ nhiều bản cần cookie này
                                        .cookie("csrf_token", csrf)                   // ✅ thêm luôn cho chắc
                                        .body(BodyInserters.fromValue(payload))
                                        .retrieve()
                                        .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                                                resp -> resp.bodyToMono(String.class)
                                                        .flatMap(b -> Mono.error(new IllegalStateException(
                                                                "Xin guest_token thất bại: " + resp.statusCode() + " " + b)))
                                        )
                                        .bodyToMono(MAP_TYPE); // { "token": "<guest_jwt>" }
                            });

                });
    }

    // Superset 3.x có thể trả String trực tiếp; bản cũ trả object { token: ... }
    @SuppressWarnings("unchecked")
    private static String extractAccessToken(Object field) {
        if (field == null) return null;
        if (field instanceof String s) return s;
        if (field instanceof Map<?, ?> m) {
            Object t = ((Map<?, ?>) m).get("token");
            return t != null ? t.toString() : null;
        }
        return null;
    }

    // Superset có version trả {"result":"<csrf>"} hoặc {"csrf_token":"<csrf>"}
    private static String extractCsrfToken(Map<String, Object> resp) {
        if (resp == null) return null;
        Object v = resp.get("csrf_token");
        if (v == null) v = resp.get("result");
        return v != null ? v.toString() : null;
    }
}
