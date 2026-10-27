package com.worksshow.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.worksshow.entity.Deployment;
import com.worksshow.mapper.RefreshTokenMapper;
import com.worksshow.service.DeploymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据维护定时任务
 * <p>
 * 1. 回收僵死部署记录:部署为同步执行,服务重启/进程被杀时记录会永久停留
 *    "部署中"(status=0),前端一直显示部署中且无法自愈。超过阈值仍处于
 *    部署中的记录统一标记为失败(实际 CLI 最长 180s + 5s 销毁等待,
 *    30 分钟阈值有充分余量)。
 * 2. 物理清理无效 refresh token(已逻辑删除/已过期),防止无限堆积。
 *
 * @author WorksShow
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupTask {

    private final DeploymentService deploymentService;
    private final RefreshTokenMapper refreshTokenMapper;

    /**
     * 每 10 分钟回收僵死部署记录(启动后 1 分钟开始)
     */
    @Scheduled(fixedDelay = 10 * 60 * 1000, initialDelay = 60 * 1000)
    public void recoverStuckDeployments() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        boolean updated = deploymentService.update(new LambdaUpdateWrapper<Deployment>()
                .eq(Deployment::getStatus, Deployment.STATUS_DEPLOYING)
                .lt(Deployment::getUpdateTime, threshold)
                .set(Deployment::getStatus, Deployment.STATUS_FAILED)
                .set(Deployment::getErrorMessage, "部署超时(服务重启或执行中断),已由系统自动标记为失败"));
        if (updated) {
            log.warn("已回收超过30分钟仍处于部署中的僵死记录");
        }
    }

    /**
     * 每天凌晨 4 点物理清理无效 refresh token
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void purgeInvalidRefreshTokens() {
        int rows = refreshTokenMapper.purgeInvalid();
        if (rows > 0) {
            log.info("已物理清理无效 refresh token: {} 行", rows);
        }
    }
}
