package com.foodlab.ncr.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.foodlab.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ncr_preventive_action")
public class NcrPreventiveAction extends BaseEntity {

    private Long ncrId;

    private String ncrCode;

    private String actionDescription;

    private String actionPlan;

    private Long actionPersonId;

    private String actionPersonName;

    private LocalDateTime planStartTime;

    private LocalDateTime planEndTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private String actionStatus;

    private String actionResult;

    private String effectivenessEvaluation;

    private Long verifyPersonId;

    private String verifyPersonName;

    private LocalDateTime verifyTime;

    private String verifyOpinion;

    private String remark;

    private String attachFiles;
}
