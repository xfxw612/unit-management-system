package com.example.unitsys.controller;

import com.example.unitsys.auth.AuthInterceptor;
import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.common.BizException;
import jakarta.servlet.http.HttpSession;

/** 从 session 取登录态。拦截器已经保证 /api/** 都登录过，这里只是取值的统一入口。 */
public abstract class BaseController {

    protected LoginUser me(HttpSession session) {
        LoginUser user = (LoginUser) session.getAttribute(AuthInterceptor.SESSION_KEY);
        if (user == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        return user;
    }
}
