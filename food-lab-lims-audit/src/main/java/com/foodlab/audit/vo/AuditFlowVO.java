package com.foodlab.audit.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuditFlowVO {

    private String processInstanceId;

    private String businessCode;

    private String currentStatus;

    private List<AuditNodeVO> nodes;

    @Data
    public static class AuditNodeVO {

        private String nodeCode;

        private String nodeName;

        private String nodeType;

        private String status;

        private Long auditorId;

        private String auditorName;

        private LocalDateTime auditTime;

        private String auditOpinion;

        private String actionType;

        private Long retestId;

        private String retestCode;
    }
}
