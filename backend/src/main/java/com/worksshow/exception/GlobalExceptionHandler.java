package com.worksshow.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import com.worksshow.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 * <p>
 * 统一捕获 Controller 层抛出的异常,转换为标准 Result 响应,
 * 避免向前端暴露堆栈信息。
 * <p>
 * 响应同时携带真实 HTTP 状态码(此前恒为 200,网关/监控/前端拦截器
 * 均无法按状态码识别失败,401 也无法触发前端的 token 刷新流程)。
 * 响应体结构 {code, message, data} 保持不变,前端兼容。
 *
 * @author WorksShow
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常: 可预期的错误,返回业务码与提示(HTTP 状态取业务码,非标准 HTTP 码时按 500) */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        int httpStatus = (e.getCode() >= 400 && e.getCode() <= 599) ? e.getCode() : 500;
        return ResponseEntity.status(httpStatus).body(Result.fail(e.getCode(), e.getMessage()));
    }

    /** Sa-Token 未登录: token 缺失/过期/非法,返回 401 */
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<Void>> handleNotLoginException(NotLoginException e) {
        log.warn("未登录或登录已失效: type={}, message={}", e.getType(), e.getMessage());
        return ResponseEntity.status(401).body(Result.fail(401, "未登录或登录已失效,请重新登录"));
    }

    /** Sa-Token 无权限: 已登录但缺少所需权限点,返回 403 */
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<Void>> handleNotPermissionException(NotPermissionException e) {
        log.warn("无权限访问: code={}", e.getCode());
        return ResponseEntity.status(403).body(Result.fail(403, "无权限访问"));
    }

    /** Sa-Token 无角色: 已登录但缺少所需角色,返回 403 */
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<Void>> handleNotRoleException(NotRoleException e) {
        log.warn("无角色访问: role={}", e.getRole());
        return ResponseEntity.status(403).body(Result.fail(403, "无角色访问"));
    }

    /** Sa-Token 其他异常兜底 */
    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<Result<Void>> handleSaTokenException(SaTokenException e) {
        log.warn("Sa-Token 异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(401).body(Result.fail(401, "认证失败,请重新登录"));
    }

    /** 参数校验异常: @Valid 校验失败时触发 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验失败: {}", message);
        return ResponseEntity.status(400).body(Result.fail(400, message));
    }

    /** 唯一约束冲突: 并发注册等场景触发(数据库唯一索引兜底),返回友好提示 */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMessage());
        return ResponseEntity.status(409).body(Result.fail(409, "数据已存在,请勿重复提交"));
    }

    /** 请求体格式错误: JSON 解析失败,返回 400 而非 500 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误: {}", e.getMessage());
        return ResponseEntity.status(400).body(Result.fail(400, "请求体格式错误"));
    }

    /** 兜底异常: 非预期错误,隐藏细节防止信息泄露 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(500).body(Result.fail("系统繁忙,请稍后重试"));
    }
}
