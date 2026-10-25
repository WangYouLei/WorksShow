package com.worksshow.dto;

import com.worksshow.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO(返回前端用)
 * <p>
 * 排除 password 与 deleted 等内部字段,仅暴露展示信息。
 *
 * @author WorksShow
 */
@Data
public class UserVO {

    /** 主键ID */
    private Long id;

    /** 邮箱(登录账号之一) */
    private String email;

    /** 手机号(登录账号之一) */
    private String phone;

    /** 昵称(展示名) */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 性别:0-未知 1-男 2-女 */
    private Integer gender;

    /** 账号状态:0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 从 Entity 转换 */
    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setGender(user.getGender());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
