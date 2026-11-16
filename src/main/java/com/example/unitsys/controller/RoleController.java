package com.example.unitsys.controller;

import com.example.unitsys.auth.RequirePerm;
import com.example.unitsys.common.ApiResponse;
import com.example.unitsys.entity.SysRole;
import com.example.unitsys.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequirePerm("role:read")
    public ApiResponse<List<SysRole>> list() {
        return ApiResponse.ok(roleService.listWithPerms());
    }

    /** 只读下拉：新增/编辑人员时要选角色，但不该逼着人家有 role:read 才能读角色名。 */
    @GetMapping("/options")
    @RequirePerm("user:read")
    public ApiResponse<List<SysRole>> options() {
        return ApiResponse.ok(roleService.listBasic());
    }
}
