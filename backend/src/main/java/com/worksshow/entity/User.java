package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 - 对应数据库 user 表
 * <p>
 * 用于登录注册功能,存储账号基本信息。
 * password 字段使用 @JsonIgnore 防止序列化返回前端。
 *
 * @author WorksShow
 */
@Data
@TableName("user")
public class User {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 密码(BCrypt 加密存储,不返回前端) */
    @JsonIgnore
    private String password;

    /** 邮箱(登录账号之一,唯一) */
    private String email;

    /** 手机号(登录账号之一,唯一) */
    private String phone;

    /** 昵称(展示名,用户自定义) */
    private String nickname;

    /** 性别:0-未知 1-男 2-女 */
    private Integer gender;

    /** 账号状态:0-禁用 1-启用 */
    private Integer status;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
