package com.example.inkstonetask.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskExecuteLog {
    private Long id;
    private Long taskId;
    private Long queueId;
    private String executeStatus;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String resultMsg;
    private String errorStack;
    private LocalDateTime createdAt;
}
