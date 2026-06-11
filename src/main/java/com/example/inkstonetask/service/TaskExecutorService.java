package com.example.inkstonetask.service;

import com.example.inkstonetask.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskExecutorService {

    public void execute(Task task) {
        log.info("execute task, taskId={}, taskName={}, executorType={}", task.getId(), task.getTaskName(), task.getExecutorType());
    }
}
