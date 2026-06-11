package com.example.inkstonetask.scheduler;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskDispatchQueue;
import com.example.inkstonetask.domain.TaskExecuteLog;
import com.example.inkstonetask.domain.TaskOperationLog;
import com.example.inkstonetask.mapper.TaskDispatchQueueMapper;
import com.example.inkstonetask.mapper.TaskExecuteLogMapper;
import com.example.inkstonetask.mapper.TaskMapper;
import com.example.inkstonetask.mapper.TaskOperationLogMapper;
import com.example.inkstonetask.service.TaskExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskWorker {

    private final TaskMapper taskMapper;
    private final TaskDispatchQueueMapper queueMapper;
    private final TaskExecuteLogMapper executeLogMapper;
    private final TaskOperationLogMapper operationLogMapper;
    private final TaskExecutorService taskExecutorService;

    private final String workerId = UUID.randomUUID().toString();

    @Scheduled(fixedDelay = 5000)
    public void consume() {
        List<TaskDispatchQueue> queueItems = queueMapper.findExecutableQueueItems(LocalDateTime.now(), 10);
        for (TaskDispatchQueue item : queueItems) {
            process(item);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void process(TaskDispatchQueue item) {
        int claimed = queueMapper.claimQueueItem(item.getId(), workerId);
        if (claimed != 1) {
            return;
        }

        Task task = taskMapper.findById(item.getTaskId());
        if (task == null) {
            queueMapper.markQueueFailed(item.getId(), "task not found");
            return;
        }

        TaskExecuteLog logRecord = new TaskExecuteLog();
        logRecord.setTaskId(task.getId());
        logRecord.setQueueId(item.getId());
        logRecord.setExecuteStatus("running");
        logRecord.setStartTime(LocalDateTime.now());
        logRecord.setCreatedAt(LocalDateTime.now());
        executeLogMapper.insert(logRecord);

        if (task.getPlanEndTime() != null && LocalDateTime.now().isAfter(task.getPlanEndTime())) {
            taskMapper.markTaskExpired(task.getId());
            queueMapper.markQueueFailed(item.getId(), "task expired");
            logRecord.setExecuteStatus("failed");
            logRecord.setEndTime(LocalDateTime.now());
            logRecord.setResultMsg("task expired");
            executeLogMapper.updateFailed(logRecord);
            writeOperationLog(task.getId(), "expired", "system", "任务已过期，停止执行");
            return;
        }

        taskMapper.markTaskRunning(task.getId());
        try {
            taskExecutorService.execute(task);
            taskMapper.markTaskFinished(task.getId());
            queueMapper.markQueueSuccess(item.getId());
            logRecord.setExecuteStatus("success");
            logRecord.setEndTime(LocalDateTime.now());
            logRecord.setResultMsg("execute success");
            executeLogMapper.updateSuccess(logRecord);
            writeOperationLog(task.getId(), "finish", "system", "任务执行成功");
        } catch (Exception e) {
            String error = stackTraceToString(e);
            if (item.getRetryCount() < task.getMaxRetryCount()) {
                taskMapper.markTaskQueuedForRetry(task.getId());
                queueMapper.markQueueRetry(item.getId(), LocalDateTime.now().plusMinutes(5), e.getMessage());
                writeOperationLog(task.getId(), "retry", "system", "任务执行失败，等待重试");
            } else {
                taskMapper.markTaskFailed(task.getId());
                queueMapper.markQueueFailed(item.getId(), e.getMessage());
                writeOperationLog(task.getId(), "failed", "system", "任务执行失败");
            }
            logRecord.setExecuteStatus("failed");
            logRecord.setEndTime(LocalDateTime.now());
            logRecord.setResultMsg(e.getMessage());
            logRecord.setErrorStack(error);
            executeLogMapper.updateFailed(logRecord);
            log.error("task execute failed, taskId={}", task.getId(), e);
        }
    }

    private void writeOperationLog(Long taskId, String type, String operator, String desc) {
        TaskOperationLog operationLog = new TaskOperationLog();
        operationLog.setTaskId(taskId);
        operationLog.setOperationType(type);
        operationLog.setOperatorName(operator);
        operationLog.setOperationDesc(desc);
        operationLogMapper.insert(operationLog);
    }

    private String stackTraceToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
