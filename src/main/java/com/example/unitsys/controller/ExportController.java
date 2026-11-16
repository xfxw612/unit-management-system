package com.example.unitsys.controller;

import com.example.unitsys.auth.RequirePerm;
import com.example.unitsys.dto.UserQuery;
import com.example.unitsys.service.ExportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 导出接口：format=csv 或 xlsx，其它查询条件与列表接口完全一致（同一套 UserQuery）。
 * 文件名走 RFC 5987 filename*，中文文件名在各浏览器都不乱码。
 */
@RestController
@RequestMapping("/api/export")
public class ExportController extends BaseController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/users")
    @RequirePerm("export:users")
    public ResponseEntity<byte[]> exportUsers(@ModelAttribute UserQuery query,
                                              @RequestParam(defaultValue = "csv") String format,
                                              HttpSession session) {
        return download(exportService.exportUsers(me(session), query, format));
    }

    @GetMapping("/dept-stats")
    @RequirePerm("export:stats")
    public ResponseEntity<byte[]> exportDeptStats(@RequestParam(defaultValue = "csv") String format,
                                                  HttpSession session) {
        return download(exportService.exportDeptStats(me(session), format));
    }

    private ResponseEntity<byte[]> download(ExportService.ExportFile file) {
        String encoded = URLEncoder.encode(file.filename(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.content().length)
                .body(file.content());
    }
}
