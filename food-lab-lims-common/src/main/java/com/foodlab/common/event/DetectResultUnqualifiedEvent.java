package com.foodlab.common.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class DetectResultUnqualifiedEvent extends ApplicationEvent {

    private Long detectResultId;
    private Long sampleId;
    private String sampleCode;
    private Long detectItemId;
    private String detectItemName;
    private Long taskId;
    private Long submitterId;
    private String submitterName;

    public DetectResultUnqualifiedEvent(Object source) {
        super(source);
    }

    public DetectResultUnqualifiedEvent(Object source, Long detectResultId,
                                         Long sampleId, String sampleCode,
                                         Long detectItemId, String detectItemName,
                                         Long taskId, Long submitterId, String submitterName) {
        super(source);
        this.detectResultId = detectResultId;
        this.sampleId = sampleId;
        this.sampleCode = sampleCode;
        this.detectItemId = detectItemId;
        this.detectItemName = detectItemName;
        this.taskId = taskId;
        this.submitterId = submitterId;
        this.submitterName = submitterName;
    }
}
