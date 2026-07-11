package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.DecryptedEdgeOneConfig;
import com.worksshow.dto.EdgeOneConfigRequestDTO;
import com.worksshow.dto.EdgeOneConfigVO;
import com.worksshow.entity.UserEdgeoneConfig;

/**
 * 用户 EdgeOne 配置服务接口
 * <p>
 * 管理用户部署到 EdgeOne Pages 所需的 API Token。
 * api_token 入库前 AES 加密,使用时解密。
 * API 响应中永不返回明文 token,仅返回脱敏串。
 *
 * @author WorksShow
 */
public interface UserEdgeoneConfigService extends IService<UserEdgeoneConfig> {

    /**
     * 获取当前用户的 EdgeOne 配置(脱敏)
     *
     * @return 脱敏配置,未配置时返回 null
     */
    EdgeOneConfigVO getMyConfig();

    /**
     * 保存或更新当前用户的 EdgeOne 配置(upsert)
     * <p>
     * 明文凭证在入库前 AES 加密。
     *
     * @param dto 明文凭证
     * @return 保存后的配置实体(含密文)
     */
    UserEdgeoneConfig saveOrUpdate(EdgeOneConfigRequestDTO dto);

    /**
     * 删除当前用户的 EdgeOne 配置(逻辑删除)
     */
    void deleteMyConfig();

    /**
     * 获取当前用户解密后的 EdgeOne 配置(明文,仅内部使用)
     * <p>
     * 供部署服务调用 EdgeOne API 时使用。严禁通过 Controller 返回。
     *
     * @return 解密后的明文凭证,未配置时抛业务异常
     */
    DecryptedEdgeOneConfig getDecryptedConfig();
}
