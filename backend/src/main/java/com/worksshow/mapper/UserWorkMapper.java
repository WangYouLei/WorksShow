package com.worksshow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worksshow.entity.UserWork;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户作品表 Mapper 接口
 * <p>
 * 继承 Mybatis-Plus 的 BaseMapper 即可获得单表 CRUD 能力。
 *
 * @author WorksShow
 */
@Mapper
public interface UserWorkMapper extends BaseMapper<UserWork> {
}
