package com.worksshow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 求职意向 DTO(保存/更新请求体)
 * <p>
 * 一个用户仅有一份求职意向,后端按 userId 做 upsert,
 * 此 DTO 不携带 id / userId(由服务端从上下文获取)。
 *
 * @author WorksShow
 */
@Data
public class CareerIntentionDTO {

    /** 求职状态:0-离职正在找 1-在职考虑机会 2-在职暂不考虑 */
    private Integer jobStatus;

    /** 期望职位(如"前端开发工程师") */
    @NotBlank(message = "期望职位不能为空")
    private String expectedPosition;

    /** 职位类别(技术/产品/设计/运营等) */
    private String positionType;

    /** 期望行业数组(如 ["互联网","金融"]) */
    private List<String> expectedIndustry;

    /** 期望城市数组(如 ["北京","上海"]) */
    private List<String> expectedCity;

    /** 工作性质:0-全职 1-兼职 2-实习 3-远程 */
    private Integer workType;

    /** 期望最低薪资(千元,如15表示15k) */
    private Integer salaryMin;

    /** 期望最高薪资(千元,如25表示25k) */
    private Integer salaryMax;

    /** 到岗时间(如"随时""一周内""一个月内") */
    private String arrivalTime;

    /** 自我评价/求职宣言(可选) */
    private String selfEvaluation;
}
