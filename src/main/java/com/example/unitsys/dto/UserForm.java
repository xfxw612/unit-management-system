package com.example.unitsys.dto;

import java.time.LocalDate;

/** 人员新增/修改的表单。password 只在新增时用；修改时不传表示不改密码。 */
public record UserForm(
        String username,
        String password,
        String realName,
        String gender,
        String phone,
        String email,
        Integer deptId,
        Integer roleId,
        String education,
        LocalDate hireDate,
        Integer status
) {
}
