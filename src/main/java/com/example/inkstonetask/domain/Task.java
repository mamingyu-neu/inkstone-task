package com.example.inkstonetask.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Task {
    private Long id;
    private String taskName;
    private String taskType;
    private String content;
    private String status;
    private Integer priority;
    private LocalDateTime planStartTime;
    private LocalDateTime planEndTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Integer maxRetryCount;
    private Integer retryCount;
    private String executorType;
    private String executorConfig;
    private Integer calendarFlag;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
