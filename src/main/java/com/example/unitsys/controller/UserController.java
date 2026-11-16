package com.example.unitsys.controller;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.auth.RequirePerm;
import com.example.unitsys.common.ApiResponse;
import com.example.unitsys.common.PageResult;
import com.example.unitsys.dto.UserForm;
import com.example.unitsys.dto.UserQuery;
import com.example.unitsys.entity.SysUser;
import com.example.unitsys.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    public record ResetPasswordForm(String password) {
    }

    public record RoleForm(Integer roleId) {
    }

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequirePerm("user:read")
    public ApiResponse<PageResult<SysUser>> page(@ModelAttribute UserQuery query, HttpSession session) {
        return ApiResponse.ok(userService.page(me(session), query));
    }

    @GetMapping("/{id}")
    @RequirePerm("user:read")
    public ApiResponse<SysUser> detail(@PathVariable int id, HttpSession session) {
        return ApiResponse.ok(userService.requireVisible(me(session), id));
    }

    @PostMapping
    @RequirePerm("user:write")
    public ApiResponse<SysUser> create(@RequestBody UserForm form, HttpSession session) {
        return ApiResponse.ok(userService.create(me(session), form));
    }

    @PutMapping("/{id}")
    @RequirePerm("user:write")
    public ApiResponse<SysUser> update(@PathVariable int id, @RequestBody UserForm form, HttpSession session) {
        return ApiResponse.ok(userService.update(me(session), id, form));
    }

    @DeleteMapping("/{id}")
    @RequirePerm("user:write")
    public ApiResponse<Void> delete(@PathVariable int id, HttpSession session) {
        userService.delete(me(session), id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/reset-password")
    @RequirePerm("user:write")
    public ApiResponse<Void> resetPassword(@PathVariable int id, @RequestBody ResetPasswordForm form, HttpSession session) {
        userService.resetPassword(me(session), id, form.password());
        return ApiResponse.ok();
    }

    /** 调整角色单独一个权限点 role:write，只有系统管理员有。 */
    @PutMapping("/{id}/role")
    @RequirePerm("role:write")
    public ApiResponse<SysUser> assignRole(@PathVariable int id, @RequestBody RoleForm form, HttpSession session) {
        LoginUser operator = me(session);
        return ApiResponse.ok(userService.assignRole(operator, id, form.roleId() == null ? 0 : form.roleId()));
    }
}
