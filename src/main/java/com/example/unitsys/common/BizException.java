package com.example.unitsys.common;

/** 业务异常：code 直接用作 HTTP 状态码，message 会原样返回给前端。 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(String message) {
        this(400, message);
    }

    public int getCode() {
        return code;
    }
}
