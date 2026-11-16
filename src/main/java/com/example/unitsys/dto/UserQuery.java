package com.example.unitsys.dto;

/**
 * 人员列表 / 导出的查询条件（从 query string 绑定）。
 * normalize() 做分页参数收敛，并把 keyword 变成 like 参数，避免在 SQL 里拼 concat。
 */
public class UserQuery {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Integer deptId;
    private Integer roleId;
    private Integer status;
    private String education;

    private String keywordLike;
    private Integer offset;

    public UserQuery normalize() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        offset = (page - 1) * size;
        keywordLike = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";
        if (keyword != null) {
            keyword = keyword.trim();
        }
        if (education != null && education.isBlank()) {
            education = null;
        }
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getKeywordLike() {
        return keywordLike;
    }

    public Integer getOffset() {
        return offset;
    }
}
