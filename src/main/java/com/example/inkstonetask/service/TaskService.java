package com.example.inkstonetask.service;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskOperationLog;
import com.example.inkstonetask.dto.TaskCreateRequest;
import com.example.inkstonetask.mapper.TaskMapper;
import com.example.inkstonetask.mapper.TaskOperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final TaskOperationLogMapper taskOperationLogMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long createTask(TaskCreateRequest request) {
        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setTaskType(request.getTaskType());
        task.setContent(request.getContent());
        task.setStatus("pending");
        task.setPriority(request.getPriority() == null ? 0 : request.getPriority());
        task.setPlanStartTime(request.getPlanStartTime());
        task.setPlanEndTime(request.getPlanEndTime());
        task.setMaxRetryCount(request.getMaxRetryCount() == null ? 3 : request.getMaxRetryCount());
        task.setRetryCount(0);
        task.setExecutorType(request.getExecutorType() == null ? "default" : request.getExecutorType());
        task.setExecutorConfig(request.getExecutorConfig() == null ? "{}" : request.getExecutorConfig());
        task.setCalendarFlag(request.getPlanStartTime() != null && request.getPlanEndTime() != null ? 1 : 0);
        task.setDeleted(0);
        taskMapper.insert(task);

        TaskOperationLog operationLog = new TaskOperationLog();
        operationLog.setTaskId(task.getId());
        operationLog.setOperationType("create");
        operationLog.setOperatorName(request.getOperatorName() == null ? "system" : request.getOperatorName());
        operationLog.setOperationDesc("创建任务");
        taskOperationLogMapper.insert(operationLog);
        return task.getId();
    }
}
