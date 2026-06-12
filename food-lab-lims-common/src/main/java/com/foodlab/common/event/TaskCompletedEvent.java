package com.foodlab.common.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class TaskCompletedEvent extends ApplicationEvent {

    private Long taskId;
    private String taskCode;
    private Long sampleId;
    private String sampleCode;
    private Long submitterId;
    private String submitterName;

    public TaskCompletedEvent(Object source) {
        super(source);
    }

    public TaskCompletedEvent(Object source, Long taskId, String taskCode,
                               Long sampleId, String sampleCode,
                               Long submitterId, String submitterName) {
        super(source);
        this.taskId = taskId;
        this.taskCode = taskCode;
        this.sampleId = sampleId;
        this.sampleCode = sampleCode;
        this.submitterId = submitterId;
        this.submitterName = submitterName;
    }
}
