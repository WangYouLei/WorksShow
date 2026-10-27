package com.worksshow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.worksshow.dto.CareerIntentionDTO;
import com.worksshow.entity.CareerIntention;
import com.worksshow.mapper.CareerIntentionMapper;
import com.worksshow.security.UserContext;
import com.worksshow.service.CareerIntentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 求职意向服务实现
 * <p>
 * 一个用户仅有一份求职意向,采用 upsert 策略:
 * 按 userId 查询,存在则更新字段,不存在则新建。
 *
 * @author WorksShow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareerIntentionServiceImpl extends ServiceImpl<CareerIntentionMapper, CareerIntention> implements CareerIntentionService {

    /**
     * 获取当前用户的求职意向
     * <p>
     * 不存在时返回 null,前端按空对象渲染。
     */
    @Override
    public CareerIntention getMine() {
        Long userId = UserContext.getCurrentUserId();
        log.info("查询当前用户求职意向: userId={}", userId);
        return getOne(new LambdaQueryWrapper<CareerIntention>()
                .eq(CareerIntention::getUserId, userId));
    }

    /**
     * 保存或更新求职意向(upsert)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareerIntention save(CareerIntentionDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        log.info("保存求职意向: userId={}", userId);

        CareerIntention entity = getOne(new LambdaQueryWrapper<CareerIntention>()
                .eq(CareerIntention::getUserId, userId));

        if (entity == null) {
            // 不存在则新建;并发首次保存会命中 userId 唯一索引兜底,
            // 此时转为查询已插入的记录并更新,而非报"请勿重复提交"
            entity = new CareerIntention();
            entity.setUserId(userId);
            applyDTO(entity, dto);
            try {
                save(entity);
                log.info("新建求职意向成功: id={}, userId={}", entity.getId(), userId);
            } catch (DuplicateKeyException e) {
                entity = getOne(new LambdaQueryWrapper<CareerIntention>()
                        .eq(CareerIntention::getUserId, userId)
                        .last("LIMIT 1"));
                if (entity == null) {
                    // 理论不可达(唯一约束冲突说明记录存在),防御性重抛
                    throw e;
                }
                applyDTO(entity, dto);
                updateById(entity);
                log.info("并发首次保存,转为更新求职意向: id={}, userId={}", entity.getId(), userId);
            }
        } else {
            // 已存在则更新
            applyDTO(entity, dto);
            updateById(entity);
            log.info("更新求职意向成功: id={}, userId={}", entity.getId(), userId);
        }
        return entity;
    }

    /**
     * 将 DTO 字段应用到实体(不覆盖 id / userId / 时间戳 / deleted)
     */
    private void applyDTO(CareerIntention entity, CareerIntentionDTO dto) {
        entity.setJobStatus(dto.getJobStatus());
        entity.setExpectedPosition(dto.getExpectedPosition());
        entity.setPositionType(dto.getPositionType());
        entity.setExpectedIndustry(dto.getExpectedIndustry());
        entity.setExpectedCity(dto.getExpectedCity());
        entity.setWorkType(dto.getWorkType());
        entity.setSalaryMin(dto.getSalaryMin());
        entity.setSalaryMax(dto.getSalaryMax());
        entity.setArrivalTime(dto.getArrivalTime());
        entity.setSelfEvaluation(dto.getSelfEvaluation());
    }
}
