package com.worksshow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.worksshow.dto.CustomDomainRequestDTO;
import com.worksshow.entity.CustomDomain;

import java.util.List;

/**
 * 用户自定义域名服务接口
 * <p>
 * 管理用户保存的可复用自定义域名,一个域名可关联多次部署
 * (多个页面通过 deployment.path 子路径区分)。
 *
 * @author WorksShow
 */
public interface CustomDomainService extends IService<CustomDomain> {

    /**
     * 获取当前用户的域名列表
     *
     * @return 域名列表,按更新时间倒序
     */
    List<CustomDomain> listMine();

    /**
     * 新增域名
     *
     * @param dto 域名数据
     * @return 创建后的域名
     */
    CustomDomain create(CustomDomainRequestDTO dto);

    /**
     * 更新域名
     *
     * @param id  域名ID
     * @param dto 域名数据
     * @return 更新后的域名
     */
    CustomDomain update(Long id, CustomDomainRequestDTO dto);

    /**
     * 删除域名(逻辑删除)
     *
     * @param id 域名ID
     */
    void delete(Long id);
}
