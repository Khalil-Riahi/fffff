package com.projet.freelencetinder.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.projet.freelencetinder.dto.*;
import com.projet.freelencetinder.servcie.AuthService;

import java.time.Duration;

/**
 * Contrôleur d’authentification :
 * - Inscription (auto-login)
 * - Login / Refresh
 * - Logout (invalidation côté client via cookies vides)
 * Les access / refresh tokens sont placés dans des cookies httpOnly.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Pas de Lombok pour rester explicite
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /* ================================================================
       1. Register (puis login automatique → cookies)
       ================================================================ */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest r) {
        authService.registerUser(r);
        // Auto-login
        TokenPair pair = authService.login(new LoginRequest(r.getEmail(), r.getPassword()));
        return buildCookieResponse(pair, HttpStatus.CREATED);
    }

    /* ================================================================
       2. Login
       ================================================================ */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest r) {
        TokenPair pair = authService.login(r);
        return buildCookieResponse(pair, HttpStatus.OK);
    }

    /* ================================================================
       3. Refresh
       (Le refresh token peut être dans le body ou (optionnellement) dans un cookie)
       ================================================================ */
    @PostMapping("/refresh-token")
    public ResponseEntity<Void> refresh(@Valid @RequestBody RefeshTokenRequest r) {
        TokenPair pair = authService.refreshToken(r);
        return buildCookieResponse(pair, HttpStatus.OK);
    }

    /* ================================================================
       4. Logout (invalide côté client en purgeant les cookies)
       ================================================================ */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return clearAuthCookies();
    }

    /* ================================================================
       5. (Optionnel) Endpoint alternatif mobile : renvoi body JSON
          Si ton front mobile ne veut pas de cookies, utilise celui-ci.
       ================================================================ */
    @PostMapping("/login/json")
    public ResponseEntity<TokenPair> loginJson(@Valid @RequestBody LoginRequest r) {
        return ResponseEntity.ok(authService.login(r));
    }

    @PostMapping("/refresh-token/json")
    public ResponseEntity<TokenPair> refreshJson(@Valid @RequestBody RefeshTokenRequest r) {
        return ResponseEntity.ok(authService.refreshToken(r));
    }

    /* ================================================================
       Helpers cookies
       ================================================================ */
//    private ResponseEntity<Void> buildCookieResponse(TokenPair p, HttpStatus status) {
//
//        // En prod HTTPS : secure = true
//        boolean secure = false; // mets true en production
//        long accessMaxAgeSeconds  = Duration.ofMinutes(15).toSeconds();   // ajustable
//        long refreshMaxAgeSeconds = Duration.ofDays(7).toSeconds();       // ajustable
//
//        ResponseCookie access = ResponseCookie.from("ACCESS_TOKEN", p.getAccessToken())
//            .httpOnly(true)
//            .secure(secure)
//            .path("/")
//            .maxAge(accessMaxAgeSeconds)
//            .sameSite("Lax")
//            .build();
//
//        ResponseCookie refresh = ResponseCookie.from("REFRESH_TOKEN", p.getRefreshToken())
//            .httpOnly(true)
//            .secure(secure)
//            .path("/api/auth/refresh-token")
//            .maxAge(refreshMaxAgeSeconds)
//            .sameSite("Lax")
//            .build();
//
//        return ResponseEntity.status(status)
//            .header(HttpHeaders.SET_COOKIE, access.toString())
//            .header(HttpHeaders.SET_COOKIE, refresh.toString())
//            .build();
//    }
//
//    private ResponseEntity<Void> clearAuthCookies() {
//        ResponseCookie expiredAccess = ResponseCookie.from("ACCESS_TOKEN", "")
//            .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();
//
//        ResponseCookie expiredRefresh = ResponseCookie.from("REFRESH_TOKEN", "")
//            .httpOnly(true).secure(false).path("/api/auth/refresh-token").maxAge(0).sameSite("Lax").build();
//
//        return ResponseEntity.ok()
//            .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
//            .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
//            .build();
//    }
    
    private ResponseEntity<Void> buildCookieResponse(TokenPair p, HttpStatus status) {

        // En prod HTTPS : secure = true
        boolean secure = false; // mets true en production
        long accessMaxAgeSeconds  = Duration.ofMinutes(15).toSeconds();   // ajustable
        long refreshMaxAgeSeconds = Duration.ofDays(7).toSeconds();       // ajustable

        ResponseCookie access = ResponseCookie.from("ACCESS_TOKEN", p.getAccessToken())
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(accessMaxAgeSeconds)
            .sameSite("Lax")
            .build();

        ResponseCookie refresh = ResponseCookie.from("REFRESH_TOKEN", p.getRefreshToken())
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(refreshMaxAgeSeconds)
            .sameSite("Lax")
            .build();

        return ResponseEntity.status(status)
            .header(HttpHeaders.SET_COOKIE, access.toString())
            .header(HttpHeaders.SET_COOKIE, refresh.toString())
            .build();
    }

    private ResponseEntity<Void> clearAuthCookies() {
        ResponseCookie expiredAccess = ResponseCookie.from("ACCESS_TOKEN", "")
            .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();

        ResponseCookie expiredRefresh = ResponseCookie.from("REFRESH_TOKEN", "")
            .httpOnly(true).secure(false).path("/api/auth/refresh-token").maxAge(0).sameSite("Lax").build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccess.toString())
            .header(HttpHeaders.SET_COOKIE, expiredRefresh.toString())
            .build();
    }

    /* ================================================================
       Gestion simple des erreurs locales (tu peux déplacer en @ControllerAdvice)
       ================================================================ */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
