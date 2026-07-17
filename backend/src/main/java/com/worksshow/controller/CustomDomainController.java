package com.worksshow.controller;

import com.worksshow.common.Result;
import com.worksshow.dto.CustomDomainRequestDTO;
import com.worksshow.dto.CustomDomainVO;
import com.worksshow.service.CustomDomainService;
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
 * 自定义域名接口
 * <p>
 * 管理用户保存的可复用自定义域名。一个域名可关联多次部署,
 * 多个页面通过 deployment.path 子路径区分。
 * 实际访问路径前缀为 /api(由 server.servlet.context-path 配置)。
 * <ul>
 *   <li>GET    /api/custom-domain/list    获取当前用户域名列表</li>
 *   <li>POST   /api/custom-domain         新增域名</li>
 *   <li>PUT    /api/custom-domain/{id}    更新域名</li>
 *   <li>DELETE /api/custom-domain/{id}    删除域名(逻辑删除)</li>
 * </ul>
 *
 * @author WorksShow
 */
@Slf4j
@RestController
@RequestMapping("/custom-domain")
@RequiredArgsConstructor
public class CustomDomainController {

    private final CustomDomainService customDomainService;

    /**
     * 获取当前用户域名列表
     */
    @GetMapping("/list")
    public Result<List<CustomDomainVO>> list() {
        log.info("获取域名列表请求");
        return Result.ok(customDomainService.listMine().stream().map(CustomDomainVO::from).toList());
    }

    /**
     * 新增域名
     */
    @PostMapping
    public Result<CustomDomainVO> create(@Valid @RequestBody CustomDomainRequestDTO dto) {
        log.info("新增域名请求: domain={}", dto.getDomain());
        return Result.ok("创建成功", CustomDomainVO.from(customDomainService.create(dto)));
    }

    /**
     * 更新域名
     */
    @PutMapping("/{id}")
    public Result<CustomDomainVO> update(@PathVariable Long id, @Valid @RequestBody CustomDomainRequestDTO dto) {
        log.info("更新域名请求: id={}", id);
        return Result.ok("更新成功", CustomDomainVO.from(customDomainService.update(id, dto)));
    }

    /**
     * 删除域名
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除域名请求: id={}", id);
        customDomainService.delete(id);
        return Result.ok("删除成功", null);
    }
}
