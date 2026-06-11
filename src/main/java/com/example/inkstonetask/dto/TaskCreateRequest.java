package com.example.inkstonetask.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskCreateRequest {
    private String taskName;
    private String taskType;
    private String content;
    private Integer priority;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private Integer maxRetryCount;
    private String executorType;
    private String executorConfig;
    private String operatorName;
}
