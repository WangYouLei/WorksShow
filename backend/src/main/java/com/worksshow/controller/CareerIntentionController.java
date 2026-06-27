package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.CareerIntentionDTO;
import com.worksshow.entity.CareerIntention;
import com.worksshow.service.CareerIntentionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 求职意向接口
 * <p>
 * 一个用户仅有一份求职意向,GET 获取、PUT 保存(upsert)。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>GET /api/career-intention  获取当前用户求职意向</li>
 *   <li>PUT /api/career-intention  保存/更新求职意向</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/career-intention")
@RequiredArgsConstructor
public class CareerIntentionController {

    private final CareerIntentionService careerIntentionService;

    /**
     * 获取当前用户求职意向
     * <p>
     * 未填写时返回 data=null,前端按空对象渲染。
     */
    @GetMapping
    public Result<CareerIntention> get() {
        log.info("获取求职意向请求");
        return Result.ok(careerIntentionService.getMine());
    }

    /**
     * 保存/更新求职意向(upsert)
     */
    @PutMapping
    public Result<CareerIntention> save(@Valid @RequestBody CareerIntentionDTO dto) {
        log.info("保存求职意向请求: expectedPosition={}", dto.getExpectedPosition());
        return Result.ok("保存成功", careerIntentionService.save(dto));
    }
}
