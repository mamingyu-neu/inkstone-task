package com.example.inkstonetask.scheduler;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskDispatchQueue;
import com.example.inkstonetask.domain.TaskOperationLog;
import com.example.inkstonetask.mapper.TaskDispatchQueueMapper;
import com.example.inkstonetask.mapper.TaskMapper;
import com.example.inkstonetask.mapper.TaskOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskScanScheduler {

    private final TaskMapper taskMapper;
    private final TaskDispatchQueueMapper queueMapper;
    private final TaskOperationLogMapper operationLogMapper;

    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void scanTasks() {
        List<Task> tasks = taskMapper.findReadyTasks();
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        for (Task task : tasks) {
            int updated = taskMapper.markTaskQueued(task.getId());
            if (updated != 1) {
                continue;
            }

            TaskDispatchQueue queue = new TaskDispatchQueue();
            queue.setTaskId(task.getId());
            queue.setQueueStatus("waiting");
            queue.setPriority(task.getPriority());
            queue.setNextTriggerTime(LocalDateTime.now());
            queue.setRetryCount(0);
            queueMapper.insertQueue(queue);

            TaskOperationLog operationLog = new TaskOperationLog();
            operationLog.setTaskId(task.getId());
            operationLog.setOperationType("system_dispatch");
            operationLog.setOperatorName("system");
            operationLog.setOperationDesc("扫描到期任务并入队");
            operationLogMapper.insert(operationLog);

            log.info("task queued, taskId={}", task.getId());
        }
    }
}
