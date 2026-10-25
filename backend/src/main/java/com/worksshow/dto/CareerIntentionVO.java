package com.worksshow.dto;

import com.worksshow.entity.CareerIntention;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 求职意向 VO(返回前端用)
 * <p>
 * 排除 userId 与 deleted 等内部字段。
 *
 * @author WorksShow
 */
@Data
public class CareerIntentionVO {

    /** 主键ID */
    private Long id;

    /** 求职状态:0-离职正在找 1-在职考虑机会 2-在职暂不考虑 */
    private Integer jobStatus;

    /** 期望职位(如"前端开发工程师") */
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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 从 Entity 转换 */
    public static CareerIntentionVO from(CareerIntention entity) {
        if (entity == null) {
            return null;
        }
        CareerIntentionVO vo = new CareerIntentionVO();
        vo.setId(entity.getId());
        vo.setJobStatus(entity.getJobStatus());
        vo.setExpectedPosition(entity.getExpectedPosition());
        vo.setPositionType(entity.getPositionType());
        vo.setExpectedIndustry(entity.getExpectedIndustry());
        vo.setExpectedCity(entity.getExpectedCity());
        vo.setWorkType(entity.getWorkType());
        vo.setSalaryMin(entity.getSalaryMin());
        vo.setSalaryMax(entity.getSalaryMax());
        vo.setArrivalTime(entity.getArrivalTime());
        vo.setSelfEvaluation(entity.getSelfEvaluation());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
