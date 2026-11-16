package com.example.unitsys.controller;

import com.example.unitsys.auth.RequirePerm;
import com.example.unitsys.common.ApiResponse;
import com.example.unitsys.entity.DeptStat;
import com.example.unitsys.entity.NameCount;
import com.example.unitsys.entity.TrendRow;
import com.example.unitsys.service.StatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController extends BaseController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 一次拿全：部门绩效、四维人员结构、月度趋势。 */
    @GetMapping("/overview")
    @RequirePerm("stats:read")
    public ApiResponse<Map<String, Object>> overview(HttpSession session) {
        return ApiResponse.ok(statsService.overview(me(session)));
    }

    @GetMapping("/dept-stats")
    @RequirePerm("stats:read")
    public ApiResponse<List<DeptStat>> deptStats(HttpSession session) {
        return ApiResponse.ok(statsService.deptStats(me(session)));
    }

    @GetMapping("/structure")
    @RequirePerm("stats:read")
    public ApiResponse<Map<String, List<NameCount>>> structure(HttpSession session) {
        return ApiResponse.ok(statsService.structure(me(session)));
    }

    @GetMapping("/trend")
    @RequirePerm("stats:read")
    public ApiResponse<List<TrendRow>> trend(HttpSession session) {
        return ApiResponse.ok(statsService.trend(me(session)));
    }
}
