package com.worksshow.service;

import com.worksshow.dto.AiTemplateGenerateRequest;
import com.worksshow.dto.AiTemplateVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 模板生成服务
 * <p>
 * 按用户设计需求生成完整单文件 HTML 模板:
 * 注入当前用户档案数据 + 生成规范 组装 Prompt,
 * 流式返回生成内容, 结束后提取清洗 HTML 并落库。
 * 产物不可表单编辑, 重新生成会写入新记录。
 *
 * @author WorksShow
 */
public interface AiTemplateService {

    /**
     * 生成模板(SSE 流式)
     * <p>
     * 事件序列: delta(多次, HTML 增量, 供前端展示进度) → done(一次, {id,name})。
     * 失败时以 error 事件结束。
     */
    SseEmitter generate(AiTemplateGenerateRequest request);

    /** 我的 AI 模板列表(不含 html, 按时间倒序) */
    List<AiTemplateVO> listMine();

    /** 模板详情(含 html), 需校验归属 */
    AiTemplateVO detail(Long id);

    /** 删除模板(物理删除, 部署记录不受影响), 需校验归属 */
    void delete(Long id);

    /**
     * 获取用于部署的简历实例ID(懒创建)。
     * <p>
     * 首次调用时创建专属 portfolio 实例(templateId=ai-generated)并回填,
     * 后续部署复用同一实例, 保证部署链接稳定。
     */
    Long ensureDeployPortfolio(Long id);
}
