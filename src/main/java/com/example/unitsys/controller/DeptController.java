package com.example.unitsys.controller;

import com.example.unitsys.auth.RequirePerm;
import com.example.unitsys.common.ApiResponse;
import com.example.unitsys.entity.SysDept;
import com.example.unitsys.service.DeptService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/depts")
public class DeptController extends BaseController {

    private final DeptService deptService;

    public DeptController(DeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping
    @RequirePerm("dept:read")
    public ApiResponse<List<SysDept>> list(HttpSession session) {
        return ApiResponse.ok(deptService.list(me(session)));
    }

    @PostMapping
    @RequirePerm("dept:write")
    public ApiResponse<SysDept> create(@RequestBody SysDept dept, HttpSession session) {
        return ApiResponse.ok(deptService.create(me(session), dept));
    }

    @PutMapping("/{id}")
    @RequirePerm("dept:write")
    public ApiResponse<SysDept> update(@PathVariable int id, @RequestBody SysDept dept, HttpSession session) {
        return ApiResponse.ok(deptService.update(me(session), id, dept));
    }

    @DeleteMapping("/{id}")
    @RequirePerm("dept:write")
    public ApiResponse<Void> delete(@PathVariable int id, HttpSession session) {
        deptService.delete(me(session), id);
        return ApiResponse.ok();
    }
}
