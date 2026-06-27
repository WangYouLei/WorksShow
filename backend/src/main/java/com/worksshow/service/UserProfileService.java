package com.worksshow.service;

import com.worksshow.dto.UserProfileDTO;

/**
 * 用户简历档案服务接口
 * <p>
 * 管理用户级简历档案(个人信息 + 作品 + 经历 + 技能),
 * 所有简历模板共享同一份档案。采用懒创建:用户首次保存时才 INSERT。
 *
 * @author WorksShow
 */
public interface UserProfileService {

    /**
     * 获取当前登录用户的档案(含 profile + works + experiences + skills)
     * <p>
     * 若用户尚未创建档案,返回空对象(各字段为 null),
     * 前端据此显示空表单让用户填写。
     *
     * @return 用户档案数据
     */
    UserProfileDTO getMyProfile();

    /**
     * 保存当前登录用户的档案(整体替换)
     * <p>
     * 采用"懒创建":若档案不存在则 INSERT,存在则 UPDATE。
     * works/experiences/skills 采用"逻辑删除旧 + 批量插入新"的整体替换策略。
     *
     * @param dto 档案数据
     * @return 保存后的档案数据
     */
    UserProfileDTO saveMyProfile(UserProfileDTO dto);
}
