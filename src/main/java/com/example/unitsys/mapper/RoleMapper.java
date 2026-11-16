package com.example.unitsys.mapper;

import com.example.unitsys.entity.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper {

    List<SysRole> list();

    SysRole findById(@Param("id") int id);

    List<String> permCodesOfRole(@Param("roleId") int roleId);
}
