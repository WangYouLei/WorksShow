package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户EdgeOne配置实体 - 对应数据库 user_edgeone_config 表
 * <p>
 * 存储用户部署到 EdgeOne Pages 所需的 API Token。
 * 一个用户一份配置,配置一次后多次部署复用。
 * api_token 在 Service 层做 AES 加解密,
 * 入库前加密、使用时解密,API 响应中永不返回明文或密文。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "user_edgeone_config", autoResultMap = true)
public class UserEdgeoneConfig {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** EdgeOne Pages API Token(AES加密存储,永不返回前端) */
    private String apiToken;

    /** EdgeOne Pages 项目名(CLI -n参数用,部署时默认项目名,明文存储) */
    private String projectName;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}
