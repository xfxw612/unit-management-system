package com.example.unitsys.mapper;

import com.example.unitsys.dto.UserQuery;
import com.example.unitsys.entity.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    List<SysUser> page(@Param("q") UserQuery q);

    long count(@Param("q") UserQuery q);

    List<SysUser> listForExport(@Param("q") UserQuery q);

    SysUser findById(@Param("id") int id);

    SysUser findByUsername(@Param("username") String username);

    int insert(SysUser user);

    int update(SysUser user);

    int deleteById(@Param("id") int id);

    int updatePassword(@Param("id") int id, @Param("password") String password);

    int updateRole(@Param("id") int id, @Param("roleId") int roleId);
}
