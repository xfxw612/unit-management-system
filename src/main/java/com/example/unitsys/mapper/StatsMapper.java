package com.example.unitsys.mapper;

import com.example.unitsys.entity.DeptStat;
import com.example.unitsys.entity.NameCount;
import com.example.unitsys.entity.TrendRow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计专用查询。deptId 为 null 表示全单位；非 null 表示只看该部门（部门管理员的数据范围）。
 */
public interface StatsMapper {

    List<DeptStat> deptStats(@Param("deptId") Integer deptId);

    List<TrendRow> trend(@Param("deptId") Integer deptId);

    List<NameCount> countByDept(@Param("deptId") Integer deptId);

    List<NameCount> countByRole(@Param("deptId") Integer deptId);

    List<NameCount> countByEducation(@Param("deptId") Integer deptId);

    List<NameCount> countByStatus(@Param("deptId") Integer deptId);
}
