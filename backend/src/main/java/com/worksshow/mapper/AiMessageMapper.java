package com.worksshow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.worksshow.entity.AiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 对话消息 Mapper 接口
 *
 * @author WorksShow
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
