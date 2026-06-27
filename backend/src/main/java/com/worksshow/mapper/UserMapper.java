package com.worksshow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worksshow.entity.User;

/**
 * 用户 Mapper 接口
 * <p>
 * 继承 Mybatis-Plus 的 BaseMapper 即可获得单表 CRUD 能力,
 * 无需手写 SQL。如需复杂查询,在此声明方法并配合 XML 或注解。
 *
 * @author WorksShow
 */
public interface UserMapper extends BaseMapper<User> {
}
