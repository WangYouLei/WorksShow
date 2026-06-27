package com.worksshow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户作品表实体 - 对应数据库 user_work 表
 * <p>
 * 用户级作品档案,所有简历模板共享。tags 以 JSON 列存储。
 * autoResultMap=true 用于让 JacksonTypeHandler 生效。
 *
 * @author WorksShow
 */
@Data
@TableName(value = "user_work", autoResultMap = true)
public class UserWork {

    /** 主键ID(数据库自增) */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID(关联 user.id) */
    private Long userId;

    /** 作品标题 */
    private String title;

    /** 分类(如"AI产品") */
    private String category;

    /** 封面图URL */
    private String cover;

    /** 作品描述 */
    private String description;

    /** 年份 */
    private String year;

    /** 外链URL */
    private String link;

    /** 技术标签数组(JSON 列,如 ["Vue","TypeScript"]) */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 是否高亮旗舰:0-否 1-是 */
    private Integer highlight;

    /** 排序序号(越小越靠前) */
    private Integer sortOrder;

    /** 创建时间(数据库自动维护) */
    private LocalDateTime createTime;

    /** 更新时间(数据库自动维护) */
    private LocalDateTime updateTime;

    /** 逻辑删除标志:0-未删除 1-已删除(Mybatis-Plus 自动处理) */
    @TableLogic
    private Integer deleted;
}
