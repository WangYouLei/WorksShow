package com.worksshow.service;

/**
 * 邮箱验证码服务接口
 * <p>
 * 提供发送验证码、校验验证码能力,用于注册邮箱验证与忘记密码重置。
 *
 * @author WorksShow
 */
public interface EmailCodeService {

    /**
     * 发送注册验证码到指定邮箱(已注册邮箱静默跳过,防滥用)
     *
     * @param email 目标邮箱
     */
    void sendCode(String email);

    /**
     * 发送重置密码验证码到指定邮箱(不做已注册检查,调用方需确保邮箱有效)
     *
     * @param email 目标邮箱
     */
    void sendCodeForReset(String email);

    /**
     * 校验验证码(校验成功后自动删除,一次性使用)
     *
     * @param email 邮箱
     * @param code  验证码
     */
    void verifyCode(String email, String code);
}
