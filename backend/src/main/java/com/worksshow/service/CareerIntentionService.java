package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.CareerIntentionDTO;
import com.worksshow.entity.CareerIntention;

/**
 * 求职意向服务接口
 * <p>
 * 继承 Mybatis-Plus 的 IService,获得单表 Service 层 CRUD 能力。
 * 额外定义获取当前用户求职意向、保存(upsert)业务方法。
 *
 * @author WorksShow
 */
public interface CareerIntentionService extends IService<CareerIntention> {

    /**
     * 获取当前用户的求职意向
     * <p>
     * 不存在时返回 null(前端按空对象渲染)。
     *
     * @return 求职意向实体,不存在返回 null
     */
    CareerIntention getMine();

    /**
     * 保存或更新求职意向(upsert)
     * <p>
     * 按当前 userId 查询:存在则更新字段,不存在则新建并设 userId。
     *
     * @param dto 求职意向数据
     * @return 保存后的实体
     */
    CareerIntention save(CareerIntentionDTO dto);
}
