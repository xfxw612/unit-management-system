package com.example.unitsys.mapper;

import com.example.unitsys.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeptMapper {

    List<SysDept> list();

    SysDept findById(@Param("id") int id);

    SysDept findByCode(@Param("code") String code);

    int insert(SysDept dept);

    int update(SysDept dept);

    int deleteById(@Param("id") int id);

    long countUsers(@Param("deptId") int deptId);
}
