package com.example.unitsys.service;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.common.BizException;
import com.example.unitsys.entity.SysUser;
import com.example.unitsys.mapper.RoleMapper;
import com.example.unitsys.mapper.UserMapper;
import com.example.unitsys.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AuthService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    public LoginUser login(String username, String rawPassword) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isEmpty()) {
            throw new BizException(400, "用户名和密码不能为空");
        }
        SysUser user = userMapper.findByUsername(username.trim());
        // 用户不存在和密码错给同一个提示，避免被用来枚举账号
        if (user == null || !PasswordUtil.matches(rawPassword, user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(403, "账号已停用，请联系管理员");
        }
        return toLoginUser(user);
    }

    public LoginUser refresh(int userId) {
        SysUser user = userMapper.findById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(401, "账号不存在或已停用");
        }
        return toLoginUser(user);
    }

    private LoginUser toLoginUser(SysUser user) {
        Set<String> perms = new LinkedHashSet<>(roleMapper.permCodesOfRole(user.getRoleId()));
        return new LoginUser(
                user.getId(), user.getUsername(), user.getRealName(),
                user.getDeptId(), user.getDeptName(),
                user.getRoleId(), user.getRoleCode(), user.getRoleName(), user.getRoleLevel(),
                perms);
    }
}
