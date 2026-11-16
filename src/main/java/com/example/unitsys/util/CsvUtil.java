package com.example.unitsys.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV 导出：带 UTF-8 BOM，Excel 双击不会把中文显示成乱码；含逗号/引号/换行的字段按 RFC 4180 加引号。
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    public static byte[] toCsv(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        appendRow(sb, headers);
        for (List<Object> row : rows) {
            appendRow(sb, row);
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void appendRow(StringBuilder sb, List<?> cells) {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(cells.get(i)));
        }
        sb.append("\r\n");
    }

    private static String escape(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.indexOf(',') >= 0 || text.indexOf('"') >= 0 || text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            return '"' + text.replace("\"", "\"\"") + '"';
        }
        return text;
    }
}
