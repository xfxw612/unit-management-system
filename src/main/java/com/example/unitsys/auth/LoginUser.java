package com.example.unitsys.auth;

import java.util.Set;

/**
 * 登录态（放在 HttpSession 里）。
 * roleLevel：1 系统管理员、2 部门管理员、3 普通员工；数字越小权限越高。
 * perms：该角色拥有的权限码，如 user:write。
 */
public record LoginUser(
        int id,
        String username,
        String realName,
        int deptId,
        String deptName,
        int roleId,
        String roleCode,
        String roleName,
        int roleLevel,
        Set<String> perms
) {
    public boolean isAdmin() {
        return roleLevel <= 1;
    }

    public boolean hasPerm(String perm) {
        return perms.contains(perm);
    }
}
