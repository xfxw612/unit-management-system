package com.example.unitsys.entity;

import java.util.ArrayList;
import java.util.List;

/** 角色（对应 sys_role）。permCodes 由 Service 组装，方便前端展示权限矩阵。 */
public class SysRole {

    private Integer id;
    private String code;
    private String name;
    private Integer level;
    private List<String> permCodes = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public List<String> getPermCodes() {
        return permCodes;
    }

    public void setPermCodes(List<String> permCodes) {
        this.permCodes = permCodes;
    }
}
