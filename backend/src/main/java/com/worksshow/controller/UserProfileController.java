package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.UserProfileDTO;
import com.worksshow.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户简历档案接口
 * <p>
 * 管理用户级简历档案(个人信息 + 作品 + 经历 + 技能),
 * 所有简历模板共享同一份档案,填一次即可在所有简历中复用。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>GET /api/user-profile  获取当前用户档案(档案不存在返回空对象)</li>
 *   <li>PUT /api/user-profile  保存当前用户档案(懒创建 + 整体替换)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/user-profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 获取当前用户档案
     * <p>
     * 档案不存在时返回空对象(各字段为 null),前端显示空表单让用户填写。
     */
    @GetMapping
    public Result<UserProfileDTO> get() {
        log.info("获取用户档案请求");
        return Result.ok(userProfileService.getMyProfile());
    }

    /**
     * 保存当前用户档案(整体替换)
     * <p>
     * 懒创建:首次保存时 INSERT,后续保存为 UPDATE。
     * works/experiences/skills 采用"逻辑删除旧 + 批量插入新"的整体替换策略。
     */
    @PutMapping
    public Result<UserProfileDTO> save(@Valid @RequestBody UserProfileDTO dto) {
        log.info("保存用户档案请求");
        return Result.ok("保存成功", userProfileService.saveMyProfile(dto));
    }
}
