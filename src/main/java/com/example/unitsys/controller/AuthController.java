package com.example.unitsys.controller;

import com.example.unitsys.auth.AuthInterceptor;
import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.common.ApiResponse;
import com.example.unitsys.dto.LoginForm;
import com.example.unitsys.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginUser> login(@RequestBody LoginForm form, HttpServletRequest request) {
        LoginUser user = authService.login(form.username(), form.password());
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthInterceptor.SESSION_KEY, user);
        return ApiResponse.ok(user);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> currentUser(HttpSession session) {
        return ApiResponse.ok(me(session));
    }
}
