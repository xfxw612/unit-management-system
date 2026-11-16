package com.example.unitsys.service;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.entity.DeptStat;
import com.example.unitsys.entity.NameCount;
import com.example.unitsys.entity.TrendRow;
import com.example.unitsys.mapper.StatsMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public Map<String, Object> overview(LoginUser me) {
        Integer deptId = scopeOf(me);
        List<DeptStat> deptStats = statsMapper.deptStats(deptId);
        deptStats.forEach(this::fillTaskRate);

        Map<String, Object> structure = new LinkedHashMap<>();
        structure.put("byDept", statsMapper.countByDept(deptId));
        structure.put("byRole", statsMapper.countByRole(deptId));
        structure.put("byEducation", statsMapper.countByEducation(deptId));
        structure.put("byStatus", statsMapper.countByStatus(deptId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scope", deptId == null ? "全单位" : me.deptName());
        result.put("deptStats", deptStats);
        result.put("structure", structure);
        result.put("trend", statsMapper.trend(deptId));
        return result;
    }

    public List<DeptStat> deptStats(LoginUser me) {
        List<DeptStat> stats = statsMapper.deptStats(scopeOf(me));
        stats.forEach(this::fillTaskRate);
        return stats;
    }

    public Map<String, List<NameCount>> structure(LoginUser me) {
        Integer deptId = scopeOf(me);
        Map<String, List<NameCount>> map = new LinkedHashMap<>();
        map.put("byDept", statsMapper.countByDept(deptId));
        map.put("byRole", statsMapper.countByRole(deptId));
        map.put("byEducation", statsMapper.countByEducation(deptId));
        map.put("byStatus", statsMapper.countByStatus(deptId));
        return map;
    }

    public List<TrendRow> trend(LoginUser me) {
        return statsMapper.trend(scopeOf(me));
    }

    /** null = 全单位；非 null = 只看该部门。 */
    private Integer scopeOf(LoginUser me) {
        return me.isAdmin() ? null : me.deptId();
    }

    private void fillTaskRate(DeptStat stat) {
        int total = stat.getTotalTasks() == null ? 0 : stat.getTotalTasks();
        int done = stat.getDoneTasks() == null ? 0 : stat.getDoneTasks();
        stat.setTaskRate(total == 0 ? 0.0 : Math.round(done * 1000.0 / total) / 10.0);
    }
}
