package com.example.unitsys.service;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.common.BizException;
import com.example.unitsys.common.PageResult;
import com.example.unitsys.dto.UserForm;
import com.example.unitsys.dto.UserQuery;
import com.example.unitsys.entity.SysUser;
import com.example.unitsys.mapper.DeptMapper;
import com.example.unitsys.mapper.RoleMapper;
import com.example.unitsys.mapper.UserMapper;
import com.example.unitsys.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员服务。这里集中三件事：分页查询、部门数据范围、角色隔离：
 * - 部门数据范围：系统管理员可看全部；部门管理员/普通员工只能看本部门，传别的 deptId 直接 403，
 *   不传则强制补成本部门。
 * - 角色隔离：非管理员只能维护 level 比自己大的角色（不能动同级或更高权限），
 *   新分配的角色 level 也必须比自己大（不能给自己/下属升成管理员）。
 */
@Service
public class UserService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final RoleMapper roleMapper;

    public UserService(UserMapper userMapper, DeptMapper deptMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.deptMapper = deptMapper;
        this.roleMapper = roleMapper;
    }

    public PageResult<SysUser> page(LoginUser me, UserQuery query) {
        UserQuery q = applyScope(me, query);
        long total = userMapper.count(q);
        List<SysUser> rows = total == 0 ? List.of() : userMapper.page(q);
        return PageResult.of(total, q.getPage(), q.getSize(), rows);
    }

    public List<SysUser> listForExport(LoginUser me, UserQuery query) {
        return userMapper.listForExport(applyScope(me, query));
    }

    public SysUser requireVisible(LoginUser me, int id) {
        SysUser user = userMapper.findById(id);
        if (user == null) {
            throw new BizException(404, "人员不存在：" + id);
        }
        if (!me.isAdmin() && !user.getDeptId().equals(me.deptId())) {
            throw new BizException(403, "越权：只能访问本部门人员");
        }
        return user;
    }

    @Transactional
    public SysUser create(LoginUser me, UserForm form) {
        validate(form, true);
        int deptId = resolveWriteDept(me, form.deptId());
        checkRoleAssignable(me, form.roleId());
        if (userMapper.findByUsername(form.username().trim()) != null) {
            throw new BizException(409, "用户名已存在：" + form.username().trim());
        }

        SysUser user = new SysUser();
        user.setUsername(form.username().trim());
        String rawPassword = (form.password() == null || form.password().isBlank()) ? DEFAULT_PASSWORD : form.password();
        user.setPassword(PasswordUtil.hash(rawPassword));
        user.setDeptId(deptId);
        applyForm(user, form);
        userMapper.insert(user);
        return userMapper.findById(user.getId());
    }

    @Transactional
    public SysUser update(LoginUser me, int id, UserForm form) {
        SysUser existing = requireVisible(me, id);
        checkTargetMutable(me, existing);
        validate(form, false);

        int deptId = resolveWriteDept(me, form.deptId());
        if (form.roleId() != null && !form.roleId().equals(existing.getRoleId())) {
            checkRoleAssignable(me, form.roleId());
        }

        existing.setDeptId(deptId);
        applyForm(existing, form);
        userMapper.update(existing);

        if (form.password() != null && !form.password().isBlank()) {
            userMapper.updatePassword(id, PasswordUtil.hash(form.password()));
        }
        return userMapper.findById(id);
    }

    @Transactional
    public void delete(LoginUser me, int id) {
        if (id == me.id()) {
            throw new BizException(400, "不能删除自己");
        }
        SysUser existing = requireVisible(me, id);
        checkTargetMutable(me, existing);
        userMapper.deleteById(id);
    }

    @Transactional
    public void resetPassword(LoginUser me, int id, String newPassword) {
        SysUser existing = requireVisible(me, id);
        checkTargetMutable(me, existing);
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException(400, "新密码至少 6 位");
        }
        userMapper.updatePassword(id, PasswordUtil.hash(newPassword));
    }

    @Transactional
    public SysUser assignRole(LoginUser me, int id, int roleId) {
        SysUser existing = requireVisible(me, id);
        if (!me.isAdmin()) {
            throw new BizException(403, "越权：只有系统管理员能直接调整角色");
        }
        if (roleMapper.findById(roleId) == null) {
            throw new BizException(400, "角色不存在：" + roleId);
        }
        userMapper.updateRole(id, roleId);
        return userMapper.findById(existing.getId());
    }

    // ---------- 校验与范围 ----------

    private UserQuery applyScope(LoginUser me, UserQuery query) {
        UserQuery q = query.normalize();
        if (!me.isAdmin()) {
            if (q.getDeptId() != null && !q.getDeptId().equals(me.deptId())) {
                throw new BizException(403, "越权：只能查看本部门人员");
            }
            q.setDeptId(me.deptId());
        }
        return q;
    }

    private int resolveWriteDept(LoginUser me, Integer formDeptId) {
        if (me.isAdmin()) {
            if (formDeptId == null) {
                throw new BizException(400, "请选择部门");
            }
            if (deptMapper.findById(formDeptId) == null) {
                throw new BizException(400, "部门不存在：" + formDeptId);
            }
            return formDeptId;
        }
        if (formDeptId != null && !formDeptId.equals(me.deptId())) {
            throw new BizException(403, "越权：只能维护本部门人员");
        }
        return me.deptId();
    }

    /** 目标人员的角色 level 必须比自己大（数字更大 = 权限更低），否则不能动他。 */
    private void checkTargetMutable(LoginUser me, SysUser target) {
        if (me.isAdmin()) {
            return;
        }
        Integer level = target.getRoleLevel();
        if (level == null || level <= me.roleLevel()) {
            throw new BizException(403, "越权：只能维护权限低于自己的成员");
        }
    }

    private void checkRoleAssignable(LoginUser me, Integer roleId) {
        if (roleId == null) {
            throw new BizException(400, "请选择角色");
        }
        if (me.isAdmin()) {
            if (roleMapper.findById(roleId) == null) {
                throw new BizException(400, "角色不存在：" + roleId);
            }
            return;
        }
        var role = roleMapper.findById(roleId);
        if (role == null) {
            throw new BizException(400, "角色不存在：" + roleId);
        }
        if (role.getLevel() <= me.roleLevel()) {
            throw new BizException(403, "越权：不能分配权限不低于自己的角色");
        }
    }

    private void validate(UserForm form, boolean creating) {
        if (form == null) {
            throw new BizException(400, "请求体不能为空");
        }
        if (creating) {
            if (form.username() == null || !form.username().trim().matches("[a-zA-Z0-9_]{3,20}")) {
                throw new BizException(400, "用户名需为 3-20 位字母/数字/下划线");
            }
            if (form.password() != null && !form.password().isBlank() && form.password().length() < 6) {
                throw new BizException(400, "密码至少 6 位");
            }
        }
        if (form.realName() == null || form.realName().isBlank()) {
            throw new BizException(400, "姓名不能为空");
        }
        if (form.deptId() == null) {
            throw new BizException(400, "请选择部门");
        }
        if (form.roleId() == null) {
            throw new BizException(400, "请选择角色");
        }
    }

    private void applyForm(SysUser user, UserForm form) {
        user.setRealName(form.realName().trim());
        user.setGender(form.gender());
        user.setPhone(form.phone());
        user.setEmail(form.email());
        user.setRoleId(form.roleId());
        user.setEducation(form.education());
        user.setHireDate(form.hireDate());
        user.setStatus(form.status() == null ? 1 : form.status());
    }
}
