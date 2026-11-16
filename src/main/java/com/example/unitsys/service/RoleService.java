package com.example.unitsys.service;

import com.example.unitsys.entity.SysRole;
import com.example.unitsys.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    /** 下拉选项用：只要 id/名称/级别，不查权限码，所以 user:read 也能取。 */
    public List<SysRole> listBasic() {
        List<SysRole> roles = roleMapper.list();
        roles.forEach(role -> role.setPermCodes(List.of()));
        return roles;
    }

    /** 角色 + 权限码，前端拿来画权限矩阵。只有 3 个角色，逐个查权限码可以接受。 */
    public List<SysRole> listWithPerms() {
        List<SysRole> roles = roleMapper.list();
        for (SysRole role : roles) {
            role.setPermCodes(roleMapper.permCodesOfRole(role.getId()));
        }
        return roles;
    }
}
