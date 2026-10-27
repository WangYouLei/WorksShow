package com.worksshow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worksshow.entity.RefreshToken;
import org.apache.ibatis.annotations.Delete;

/**
 * Refresh Token Mapper 接口
 * <p>
 * 继承 Mybatis-Plus 的 BaseMapper 即可获得单表 CRUD 能力。
 *
 * @author WorksShow
 */
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    /**
     * 物理清理无效 refresh token(已逻辑删除或已过期)。
     * <p>
     * 实体的 @TableLogic 会给 wrapper 查询自动追加 deleted=0,
     * 无法用 Service API 触达 deleted=1 的行,故用原生 SQL。
     *
     * @return 删除行数
     */
    @Delete("DELETE FROM refresh_token WHERE deleted = 1 OR expires_at < NOW()")
    int purgeInvalid();
}
