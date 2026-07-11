package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.dto.CustomDomainRequestDTO;
import com.worksshow.entity.CustomDomain;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.CustomDomainMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.CustomDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户自定义域名服务实现
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomDomainServiceImpl extends ServiceImpl<CustomDomainMapper, CustomDomain> implements CustomDomainService {

    /**
     * 获取当前用户的域名列表
     */
    @Override
    public List<CustomDomain> listMine() {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询用户域名列表: userId={}", userId);
        return list(new LambdaQueryWrapper<CustomDomain>()
                .eq(CustomDomain::getUserId, userId)
                .orderByDesc(CustomDomain::getUpdateTime));
    }

    /**
     * 新增域名
     */
    @Override
    public CustomDomain create(CustomDomainRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        CustomDomain domain = new CustomDomain();
        domain.setUserId(userId);
        domain.setDomain(dto.getDomain());
        domain.setName(dto.getName());
        save(domain);
        log.info("新增域名成功: id={}, userId={}", domain.getId(), userId);
        return domain;
    }

    /**
     * 更新域名
     */
    @Override
    public CustomDomain update(Long id, CustomDomainRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        CustomDomain domain = getById(id);
        if (domain == null) {
            throw new BusinessException(404, "域名不存在");
        }
        checkOwnership(domain);
        domain.setDomain(dto.getDomain());
        domain.setName(dto.getName());
        updateById(domain);
        log.info("更新域名成功: id={}, userId={}", id, userId);
        return domain;
    }

    /**
     * 删除域名(逻辑删除)
     */
    @Override
    public void delete(Long id) {
        Long userId = UserContext.getCurrentUserId();
        CustomDomain domain = getById(id);
        if (domain == null) {
            log.warn("删除失败,域名不存在: id={}", id);
            throw new BusinessException(404, "域名不存在");
        }
        checkOwnership(domain);
        removeById(id);
        log.info("删除域名成功: id={}, userId={}", id, userId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验域名归属当前登录用户
     */
    private void checkOwnership(CustomDomain domain) {
        Long userId = UserContext.getCurrentUserId();
        if (!domain.getUserId().equals(userId)) {
            log.warn("越权操作域名: domainId={}, ownerId={}, currentUserId={}",
                    domain.getId(), domain.getUserId(), userId);
            throw new BusinessException(403, "无权操作该域名");
        }
    }
}
