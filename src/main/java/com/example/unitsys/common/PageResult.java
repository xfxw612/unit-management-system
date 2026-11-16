package com.example.unitsys.common;

import java.util.List;

/** 分页结果：rows 是当前页，total 是满足条件的总条数。 */
public record PageResult<T>(long total, int page, int size, List<T> rows) {

    public static <T> PageResult<T> of(long total, int page, int size, List<T> rows) {
        return new PageResult<>(total, page, size, rows);
    }
}
