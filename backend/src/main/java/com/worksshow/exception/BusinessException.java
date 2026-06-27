package com.worksshow.exception;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于在业务流程中主动抛出可预期的错误(如:用户名已存在、密码错误)。
 * 由 GlobalExceptionHandler 统一捕获后转换为友好的响应。
 *
 * @author WorksShow
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
