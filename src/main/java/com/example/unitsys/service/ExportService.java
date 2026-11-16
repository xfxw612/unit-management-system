package com.example.unitsys.service;

import com.example.unitsys.auth.LoginUser;
import com.example.unitsys.dto.UserQuery;
import com.example.unitsys.entity.DeptStat;
import com.example.unitsys.entity.SysUser;
import com.example.unitsys.util.CsvUtil;
import com.example.unitsys.util.ExcelUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 导出：同样的查询条件，能出 CSV 也能出 xlsx。 */
@Service
public class ExportService {

    public record ExportFile(byte[] content, String filename, String contentType) {
    }

    private final UserService userService;
    private final StatsService statsService;

    public ExportService(UserService userService, StatsService statsService) {
        this.userService = userService;
        this.statsService = statsService;
    }

    public ExportFile exportUsers(LoginUser me, UserQuery query, String format) {
        List<SysUser> users = userService.listForExport(me, query);
        List<String> headers = List.of("工号", "姓名", "部门", "角色", "性别", "学历", "入职日期", "状态", "绩效均分");
        List<List<Object>> rows = new ArrayList<>();
        for (SysUser u : users) {
            rows.add(Arrays.asList(
                    u.getUsername(),
                    u.getRealName(),
                    u.getDeptName(),
                    u.getRoleName(),
                    u.getGender(),
                    u.getEducation(),
                    u.getHireDate() == null ? "" : u.getHireDate().toString(),
                    u.getStatus() != null && u.getStatus() == 1 ? "在职" : "离职",
                    u.getAvgScore()
            ));
        }
        return build("人员清单", headers, rows, format);
    }

    public ExportFile exportDeptStats(LoginUser me, String format) {
        List<DeptStat> stats = statsService.deptStats(me);
        List<String> headers = List.of("部门", "负责人", "在职人数", "绩效均分", "任务完成率(%)", "任务总数", "已完成");
        List<List<Object>> rows = new ArrayList<>();
        for (DeptStat s : stats) {
            rows.add(Arrays.asList(
                    s.getDeptName(),
                    s.getLeader(),
                    s.getHeadcount(),
                    s.getAvgScore(),
                    s.getTaskRate(),
                    s.getTotalTasks(),
                    s.getDoneTasks()
            ));
        }
        return build("部门绩效", headers, rows, format);
    }

    private ExportFile build(String baseName, List<String> headers, List<List<Object>> rows, String format) {
        String day = LocalDate.now().toString();
        if ("xlsx".equalsIgnoreCase(format)) {
            return new ExportFile(
                    ExcelUtil.toXlsx(baseName, headers, rows),
                    baseName + "-" + day + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return new ExportFile(
                CsvUtil.toCsv(headers, rows),
                baseName + "-" + day + ".csv",
                "text/csv;charset=UTF-8");
    }
}
