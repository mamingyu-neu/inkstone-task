package com.example.inkstonetask.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskOperationLog {
    private Long id;
    private Long taskId;
    private String operationType;
    private String operatorName;
    private String operationDesc;
    private LocalDateTime createdAt;
}
