package com.worksshow.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限/角色查询实现
 * <p>
 * 当前项目为单角色用户系统(已登录即普通用户),权限与角色均返回空列表。
 * 后续若引入管理员/访客分级,可在此处根据 loginId 查库返回角色与权限点,
 * 配合 {@code @SaCheckRole} / {@code @SaCheckPermission} 注解使用。
 *
 * @author WorksShow
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
