package com.shootdoori.match.controller;

import com.shootdoori.match.dto.AuthToken;
import com.shootdoori.match.dto.LoginRequest;
import com.shootdoori.match.dto.ProfileCreateRequest;
import com.shootdoori.match.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@RequestBody LoginRequest loginRequest) {
        AuthToken token = authService.login(loginRequest);

//        // Refresh Token은 HttpOnly 쿠키에 담아 응답
//        Cookie cookie = new Cookie("refreshToken", token.getRefreshToken());
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true); // HTTPS 환경에서만 사용
//        cookie.setPath("/");
//        cookie.setMaxAge(....);
//        response.addCookie(cookie);

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthToken> register(@RequestBody ProfileCreateRequest profileCreateRequest) {
        AuthToken token = authService.register(profileCreateRequest);
        return ResponseEntity.ok(token);
    }
}