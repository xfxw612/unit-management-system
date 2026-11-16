package com.example.unitsys.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 两道闸门（都在进 Controller 之前）：
 * 1) 登录校验：/api/** 除登录接口外必须有 session 里的 LoginUser；
 * 2) 权限校验：方法上有 @RequirePerm("xxx")，角色权限码里没有 xxx 就 403。
 * 部门数据范围（只能看本部门）在 Service 里做，因为那要看参数，见 UserService#applyScope。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "loginUser";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        LoginUser me = currentUser(request);
        if (me == null) {
            writeJson(response, 401, "未登录或登录已过期");
            return false;
        }

        RequirePerm requirePerm = handlerMethod.getMethodAnnotation(RequirePerm.class);
        if (requirePerm == null) {
            requirePerm = handlerMethod.getBeanType().getAnnotation(RequirePerm.class);
        }
        if (requirePerm != null && !me.hasPerm(requirePerm.value())) {
            writeJson(response, 403, "无权限：" + requirePerm.value());
            return false;
        }
        return true;
    }

    public static LoginUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (LoginUser) session.getAttribute(SESSION_KEY);
    }

    private static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        String escaped = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + escaped + "\"}");
    }
}
