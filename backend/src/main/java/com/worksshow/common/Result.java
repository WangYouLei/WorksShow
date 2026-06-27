package com.worksshow.common;

import lombok.Data;

/**
 * 统一响应结果封装
 * <p>
 * 所有 Controller 接口统一返回此结构,前端按 code 判断成败。
 * code = 200 表示成功,其余为失败。
 *
 * @param <T> 业务数据类型
 * @author WorksShow
 */
@Data
public class Result<T> {

    /** 状态码: 200 成功,其他失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功(无数据) */
    public static <T> Result<T> ok() {
        return new Result<>(200, "success", null);
    }

    /** 成功(带数据) */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功(自定义提示 + 数据) */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data);
    }

    /** 失败(默认 500) */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /** 失败(自定义状态码) */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
