package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.PortfolioDataDTO;
import com.worksshow.dto.PortfolioRequestDTO;
import com.worksshow.entity.Portfolio;

import java.util.List;

/**
 * 简历服务接口
 * <p>
 * 继承 Mybatis-Plus 的 IService,获得单表 Service 层 CRUD 能力。
 * 仅管理"简历实例"(portfolio 表):用户启用了哪个模板 + 简历名称。
 * 简历内容(个人信息/作品/经历/技能)由 UserProfileService 管理。
 *
 * @author WorksShow
 */
public interface PortfolioService extends IService<Portfolio> {

    /**
     * 获取当前用户的简历实例列表
     *
     * @return 简历列表,按更新时间倒序
     */
    List<Portfolio> listMine();

    /**
     * 获取简历详情(portfolio 实例 + 用户档案内容,用于渲染)
     * <p>
     * 需校验简历归属于当前登录用户。
     *
     * @param id 简历ID
     * @return 简历详情(实例元数据 + 用户档案)
     */
    PortfolioDataDTO getDetail(Long id);

    /**
     * 创建简历实例
     *
     * @param dto 简历实例数据(模板ID + 简历名称)
     * @return 创建后的简历实例
     */
    Portfolio create(PortfolioRequestDTO dto);

    /**
     * 更新简历实例(仅 name 字段,templateId 不可变)
     * <p>
     * 需校验简历归属于当前登录用户。
     *
     * @param id  简历ID
     * @param dto 简历实例数据
     * @return 更新后的简历实例
     */
    Portfolio update(Long id, PortfolioRequestDTO dto);

    /**
     * 删除简历实例(逻辑删除)
     * <p>
     * 需校验简历归属于当前登录用户。用户档案不受影响。
     *
     * @param id 简历ID
     */
    void delete(Long id);
}
