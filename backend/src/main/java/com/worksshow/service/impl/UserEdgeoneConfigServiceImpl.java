package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.common.AesUtils;
import com.worksshow.dto.DecryptedEdgeOneConfig;
import com.worksshow.dto.EdgeOneConfigRequestDTO;
import com.worksshow.dto.EdgeOneConfigVO;
import com.worksshow.entity.UserEdgeoneConfig;
import com.worksshow.exception.BusinessException;
import com.worksshow.mapper.UserEdgeoneConfigMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.UserEdgeoneConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户 EdgeOne 配置服务实现
 * <p>
 * 管理用户 EdgeOne Pages API Token:入库前 AES 加密,使用时解密,
 * 查询返回脱敏信息。api_token 永不出现在响应明文中。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEdgeoneConfigServiceImpl extends ServiceImpl<UserEdgeoneConfigMapper, UserEdgeoneConfig> implements UserEdgeoneConfigService {

    private final AesUtils aesUtils;

    /**
     * 获取当前用户的配置(脱敏)
     */
    @Override
    public EdgeOneConfigVO getMyConfig() {
        Long userId = UserContext.getCurrentUserId();
        UserEdgeoneConfig config = getOne(new LambdaQueryWrapper<UserEdgeoneConfig>()
                .eq(UserEdgeoneConfig::getUserId, userId));
        if (config == null) {
            return null;
        }
        EdgeOneConfigVO vo = new EdgeOneConfigVO();
        vo.setId(config.getId());
        // 直接掩码密文,无需解密(避免明文暴露,符合"only a masked display string is returned"约束)
        vo.setApiTokenMasked(maskToken(config.getApiToken()));
        vo.setProjectName(config.getProjectName());
        vo.setCreateTime(config.getCreateTime());
        vo.setUpdateTime(config.getUpdateTime());
        return vo;
    }

    /**
     * 保存或更新配置(upsert)
     * <p>
     * 明文 API Token 入库前 AES 加密。
     * 更新场景下 apiToken 可留空(仅更新 projectName);首次配置时 apiToken 必填。
     */
    @Override
    public UserEdgeoneConfig saveOrUpdate(EdgeOneConfigRequestDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        UserEdgeoneConfig existing = getOne(new LambdaQueryWrapper<UserEdgeoneConfig>()
                .eq(UserEdgeoneConfig::getUserId, userId));

        boolean hasToken = dto.getApiToken() != null && !dto.getApiToken().isBlank();

        if (existing != null) {
            // 更新:apiToken 留空则仅更新 projectName
            if (hasToken) {
                existing.setApiToken(aesUtils.encrypt(dto.getApiToken()));
            }
            existing.setProjectName(dto.getProjectName());
            updateById(existing);
            log.info("更新EdgeOne配置: userId={}, tokenChanged={}", userId, hasToken);
            return existing;
        }

        // 首次配置:apiToken 必填
        if (!hasToken) {
            throw new BusinessException(400, "首次配置必须填写 API Token");
        }
        UserEdgeoneConfig config = new UserEdgeoneConfig();
        config.setUserId(userId);
        config.setApiToken(aesUtils.encrypt(dto.getApiToken()));
        config.setProjectName(dto.getProjectName());
        save(config);
        log.info("保存EdgeOne配置: userId={}", userId);
        return config;
    }

    /**
     * 删除当前用户的配置(逻辑删除)
     */
    @Override
    public void deleteMyConfig() {
        Long userId = UserContext.getCurrentUserId();
        UserEdgeoneConfig config = getOne(new LambdaQueryWrapper<UserEdgeoneConfig>()
                .eq(UserEdgeoneConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(404, "EdgeOne配置不存在");
        }
        removeById(config.getId());
        log.info("删除EdgeOne配置: userId={}", userId);
    }

    /**
     * 获取解密后的明文配置(供部署服务内部使用)
     */
    @Override
    public DecryptedEdgeOneConfig getDecryptedConfig() {
        Long userId = UserContext.getCurrentUserId();
        UserEdgeoneConfig config = getOne(new LambdaQueryWrapper<UserEdgeoneConfig>()
                .eq(UserEdgeoneConfig::getUserId, userId));
        if (config == null) {
            throw new BusinessException(400, "请先配置EdgeOne API Token");
        }
        return new DecryptedEdgeOneConfig(
                aesUtils.decrypt(config.getApiToken()),
                config.getProjectName()
        );
    }

    // ==================== 私有辅助方法 ====================

    /**
     * API Token(密文)脱敏:保留首尾各 4 位,中间用 **** 替代。
     * 仅用于前端展示"已配置"状态,无需还原明文。
     */
    private String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        if (token.length() <= 8) {
            return token.substring(0, Math.min(2, token.length())) + "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
