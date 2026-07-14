package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模板部署记录实体 - 对应数据库 deployment 表
 * <p>
 * 用户将简历实例(portfolio)部署到 EdgeOne Pages 的记录。
 * 每次部署生成一条记录,关联一个简历实例。
 * 部署为同步过程,HTTP 请求阻塞等待 CLI 执行完成(通常数秒至数十秒)。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "deployment", autoResultMap = true)
public class Deployment {

    /** 部署状态:0-部署中 1-成功 2-失败 */
    public static final int STATUS_DEPLOYING = 0;
    /** 部署状态:1-成功 */
    public static final int STATUS_SUCCESS = 1;
    /** 部署状态:2-失败 */
    public static final int STATUS_FAILED = 2;

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 关联简历实例ID(关联 portfolio.id) */
    private Long portfolioId;

    /** 模板ID(aurora/mo-yun/pastel,冗余便于查询) */
    private String templateId;

    /** 部署项目名称(用户自定义,展示用) */
    private String projectName;

    /** 部署描述(用户可选填写) */
    private String description;

    /** 关联自定义域名ID(关联 custom_domain.id,NULL表示用EdgeOne默认域名) */
    private Long customDomainId;

    /** 域名下子路径(同一域名区分多页面,如/portfolio1) */
    private String path;

    /** 部署状态:0-部署中 1-成功 2-失败 */
    private Integer status;

    /** 部署成功后的完整访问URL(如myresume.edgeone.app/portfolio1) */
    private String deployUrl;

    /** 部署失败时的错误信息 */
    private String errorMessage;

    /** 创建时间(部署发起时间,数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(部署完成时间,数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}
