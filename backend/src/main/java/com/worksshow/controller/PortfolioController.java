package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.PortfolioDataDTO;
import com.worksshow.dto.PortfolioRequestDTO;
import com.worksshow.dto.PortfolioVO;
import com.worksshow.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 简历接口
 * <p>
 * 管理简历实例(用户启用的模板 + 简历名称)。
 * 简历内容(个人信息/作品/经历/技能)由 /api/user-profile 接口管理,
 * 所有模板共享同一份用户档案。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>GET    /api/portfolio/list    获取当前用户简历实例列表</li>
 *   <li>GET    /api/portfolio/{id}    获取简历详情(实例元数据 + 用户档案,用于渲染)</li>
 *   <li>POST   /api/portfolio         创建简历实例(选模板 + 命名)</li>
 *   <li>PUT    /api/portfolio/{id}    更新简历实例(仅名称)</li>
 *   <li>DELETE /api/portfolio/{id}    删除简历实例(逻辑删除)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 获取当前用户简历实例列表
     */
    @GetMapping("/list")
    public Result<List<PortfolioVO>> list() {
        log.info("获取简历列表请求");
        return Result.ok(portfolioService.listMine().stream().map(PortfolioVO::from).toList());
    }

    /**
     * 获取简历详情(实例元数据 + 用户档案内容,用于渲染)
     */
    @GetMapping("/{id}")
    public Result<PortfolioDataDTO> get(@PathVariable Long id) {
        log.info("获取简历详情请求: id={}", id);
        return Result.ok(portfolioService.getDetail(id));
    }

    /**
     * 创建简历实例
     */
    @PostMapping
    public Result<PortfolioVO> create(@Valid @RequestBody PortfolioRequestDTO dto) {
        log.info("创建简历请求: name={}, templateId={}", dto.getName(), dto.getTemplateId());
        return Result.ok("创建成功", PortfolioVO.from(portfolioService.create(dto)));
    }

    /**
     * 更新简历实例(仅名称,templateId 不可变)
     */
    @PutMapping("/{id}")
    public Result<PortfolioVO> update(@PathVariable Long id, @Valid @RequestBody PortfolioRequestDTO dto) {
        log.info("更新简历请求: id={}", id);
        return Result.ok("更新成功", PortfolioVO.from(portfolioService.update(id, dto)));
    }

    /**
     * 删除简历实例(逻辑删除)
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除简历请求: id={}", id);
        portfolioService.delete(id);
        return Result.ok("删除成功", null);
    }
}
