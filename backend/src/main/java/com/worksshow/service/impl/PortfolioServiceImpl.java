package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.dto.PortfolioDataDTO;
import com.worksshow.dto.PortfolioRequestDTO;
import com.worksshow.dto.UserProfileDTO;
import com.worksshow.entity.Portfolio;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.PortfolioMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.PortfolioService;
import com.worksshow.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简历服务实现
 * <p>
 * 仅管理"简历实例"(portfolio 表):用户启用了哪个模板 + 简历名称。
 * 简历内容(个人信息/作品/经历/技能)由 UserProfileService 提供,
 * getDetail 时合并两者返回完整渲染数据。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl extends ServiceImpl<PortfolioMapper, Portfolio> implements PortfolioService {

    private final UserProfileService userProfileService;

    /**
     * 获取当前用户的简历实例列表
     */
    @Override
    public List<Portfolio> listMine() {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询用户简历列表: userId={}", userId);
        return list(new LambdaQueryWrapper<Portfolio>()
                .eq(Portfolio::getUserId, userId)
                .orderByDesc(Portfolio::getUpdateTime));
    }

    /**
     * 获取简历详情(portfolio 实例 + 用户档案)
     */
    @Override
    public PortfolioDataDTO getDetail(Long id) {
        log.info("查询简历详情: id={}, userId={}", id, UserContext.getCurrentUserId());

        Portfolio portfolio = getById(id);
        if (portfolio == null) {
            log.warn("简历不存在: id={}", id);
            throw new BusinessException(404, "简历不存在");
        }
        checkOwnership(portfolio);

        // 组装 DTO:实例元数据 + 用户档案内容
        PortfolioDataDTO dto = new PortfolioDataDTO();
        dto.setId(portfolio.getId());
        dto.setTemplateId(portfolio.getTemplateId());
        dto.setName(portfolio.getName());

        UserProfileDTO profile = userProfileService.getMyProfile();
        dto.setProfile(profile.getProfile());
        dto.setWorks(profile.getWorks());
        dto.setExperiences(profile.getExperiences());
        dto.setSkills(profile.getSkills());
        return dto;
    }

    /**
     * 创建简历实例
     * <p>
     * 去重:同一用户同一模板只允许一个未删除实例。若已存在则返回已有实例
     * (并按需更新 name),避免跨会话/并发重复创建。
     * 注:数据库层不加 UNIQUE(user_id, template_id, deleted) 是因为
     * 逻辑删除会产生多条 (user_id, template_id, 1) 记录导致冲突,
     * 改用应用层检查更灵活。
     */
    @Override
    public Portfolio create(PortfolioRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        Portfolio existing = getOne(new LambdaQueryWrapper<Portfolio>()
                .eq(Portfolio::getUserId, userId)
                .eq(Portfolio::getTemplateId, dto.getTemplateId()));
        if (existing != null) {
            log.info("模板实例已存在,返回已有实例: id={}, userId={}, templateId={}",
                    existing.getId(), userId, dto.getTemplateId());
            if (dto.getName() != null && !dto.getName().equals(existing.getName())) {
                existing.setName(dto.getName());
                updateById(existing);
            }
            return existing;
        }
        Portfolio portfolio = new Portfolio();
        portfolio.setUserId(userId);
        portfolio.setTemplateId(dto.getTemplateId());
        portfolio.setName(dto.getName());
        save(portfolio);
        log.info("创建简历成功: id={}, userId={}", portfolio.getId(), userId);
        return portfolio;
    }

    /**
     * 更新简历实例(仅 name)
     */
    @Override
    public Portfolio update(Long id, PortfolioRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        Portfolio portfolio = getById(id);
        if (portfolio == null) {
            throw new BusinessException(404, "简历不存在");
        }
        checkOwnership(portfolio);
        portfolio.setName(dto.getName());
        updateById(portfolio);
        log.info("更新简历成功: id={}, userId={}", id, userId);
        return portfolio;
    }

    /**
     * 删除简历实例(逻辑删除)
     * <p>
     * 用户档案不受影响:删除简历不会清空档案,档案由 /api/user-profile 单独管理。
     */
    @Override
    public void delete(Long id) {
        Long userId = UserContext.getCurrentUserId();
        Portfolio portfolio = getById(id);
        if (portfolio == null) {
            log.warn("删除失败,简历不存在: id={}", id);
            throw new BusinessException(404, "简历不存在");
        }
        checkOwnership(portfolio);
        removeById(id);
        log.info("删除简历成功: id={}, userId={}", id, userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验简历归属当前登录用户
     */
    private void checkOwnership(Portfolio portfolio) {
        Long userId = UserContext.getCurrentUserId();
        if (!portfolio.getUserId().equals(userId)) {
            log.warn("越权操作简历: portfolioId={}, ownerId={}, currentUserId={}",
                    portfolio.getId(), portfolio.getUserId(), userId);
            throw new BusinessException(403, "无权操作该简历");
        }
    }
}
