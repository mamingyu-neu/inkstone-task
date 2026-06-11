package com.example.inkstonetask.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDispatchQueue {
    private Long id;
    private Long taskId;
    private String queueStatus;
    private Integer priority;
    private LocalDateTime nextTriggerTime;
    private String workerId;
    private Integer retryCount;
    private String failReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
