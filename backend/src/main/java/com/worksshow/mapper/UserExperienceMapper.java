package com.worksshow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worksshow.entity.UserExperience;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户经历表 Mapper 接口
 * <p>
 * 继承 Mybatis-Plus 的 BaseMapper 即可获得单表 CRUD 能力。
 *
 * @author WorksShow
 */
@Mapper
public interface UserExperienceMapper extends BaseMapper<UserExperience> {

    /**
     * 物理删除某用户的所有经历(整体替换时清理旧记录,绕过逻辑删除避免软删除数据累积)。
     *
     * @param userId 用户ID
     * @return 删除行数
     */
    @Delete("DELETE FROM user_experience WHERE user_id = #{userId}")
    int deleteByUserIdPhysically(@Param("userId") Long userId);
}
