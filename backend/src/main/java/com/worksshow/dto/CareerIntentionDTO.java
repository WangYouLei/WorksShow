package com.worksshow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Min(value = 0, message = "求职状态值不合法")
    @Max(value = 2, message = "求职状态值不合法")
    private Integer jobStatus;

    /** 期望职位(如"前端开发工程师") */
    @NotBlank(message = "期望职位不能为空")
    @Size(max = 100, message = "期望职位长度不能超过100")
    private String expectedPosition;

    /** 职位类别(技术/产品/设计/运营等) */
    @Size(max = 50, message = "职位类别长度不能超过50")
    private String positionType;

    /** 期望行业数组(如 ["互联网","金融"]) */
    private List<String> expectedIndustry;

    /** 期望城市数组(如 ["北京","上海"]) */
    private List<String> expectedCity;

    /** 工作性质:0-全职 1-兼职 2-实习 3-远程 */
    @Min(value = 0, message = "工作性质值不合法")
    @Max(value = 3, message = "工作性质值不合法")
    private Integer workType;

    /** 期望最低薪资(千元,如15表示15k) */
    @Min(value = 0, message = "期望最低薪资不能为负数")
    private Integer salaryMin;

    /** 期望最高薪资(千元,如25表示25k) */
    @Min(value = 0, message = "期望最高薪资不能为负数")
    private Integer salaryMax;

    /** 到岗时间(如"随时""一周内""一个月内") */
    @Size(max = 50, message = "到岗时间长度不能超过50")
    private String arrivalTime;

    /** 自我评价/求职宣言(可选) */
    @Size(max = 2000, message = "自我评价长度不能超过2000")
    private String selfEvaluation;
}
