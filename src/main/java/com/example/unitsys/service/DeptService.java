package com.example.unitsys.service;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.common.BizException;
import com.example.unitsys.entity.SysDept;
import com.example.unitsys.mapper.DeptMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeptService {

    private final DeptMapper deptMapper;

    public DeptService(DeptMapper deptMapper) {
        this.deptMapper = deptMapper;
    }

    /** 管理员看全部部门；其余角色只看自己部门（数据范围在服务端收口，不靠前端隐藏）。 */
    public List<SysDept> list(LoginUser me) {
        List<SysDept> all = deptMapper.list();
        if (me.isAdmin()) {
            return all;
        }
        return all.stream().filter(d -> d.getId().equals(me.deptId())).toList();
    }

    @Transactional
    public SysDept create(LoginUser me, SysDept dept) {
        requireAdmin(me);
        validate(dept);
        if (deptMapper.findByCode(dept.getCode().trim()) != null) {
            throw new BizException(409, "部门编码已存在：" + dept.getCode());
        }
        dept.setCode(dept.getCode().trim());
        dept.setName(dept.getName().trim());
        if (dept.getSort() == null) {
            dept.setSort(99);
        }
        deptMapper.insert(dept);
        return deptMapper.findById(dept.getId());
    }

    @Transactional
    public SysDept update(LoginUser me, int id, SysDept form) {
        requireAdmin(me);
        SysDept existing = deptMapper.findById(id);
        if (existing == null) {
            throw new BizException(404, "部门不存在：" + id);
        }
        validate(form);
        SysDept sameCode = deptMapper.findByCode(form.getCode().trim());
        if (sameCode != null && !sameCode.getId().equals(id)) {
            throw new BizException(409, "部门编码已存在：" + form.getCode());
        }
        existing.setCode(form.getCode().trim());
        existing.setName(form.getName().trim());
        existing.setParentId(form.getParentId());
        existing.setLeader(form.getLeader());
        existing.setSort(form.getSort() == null ? existing.getSort() : form.getSort());
        deptMapper.update(existing);
        return deptMapper.findById(id);
    }

    @Transactional
    public void delete(LoginUser me, int id) {
        requireAdmin(me);
        if (id == me.deptId()) {
            throw new BizException(400, "不能删除自己所在的部门");
        }
        if (deptMapper.findById(id) == null) {
            throw new BizException(404, "部门不存在：" + id);
        }
        long users = deptMapper.countUsers(id);
        if (users > 0) {
            throw new BizException(409, "该部门还有 " + users + " 名人员，不能删除");
        }
        deptMapper.deleteById(id);
    }

    private void requireAdmin(LoginUser me) {
        if (!me.isAdmin()) {
            throw new BizException(403, "越权：只有系统管理员能维护部门");
        }
    }

    private void validate(SysDept dept) {
        if (dept.getCode() == null || dept.getCode().isBlank()) {
            throw new BizException(400, "部门编码不能为空");
        }
        if (dept.getName() == null || dept.getName().isBlank()) {
            throw new BizException(400, "部门名称不能为空");
        }
    }
}
