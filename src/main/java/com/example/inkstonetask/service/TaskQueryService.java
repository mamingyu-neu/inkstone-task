package com.example.inkstonetask.service;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskDispatchQueue;
import com.example.inkstonetask.domain.TaskExecuteLog;
import com.example.inkstonetask.mapper.TaskDispatchQueueMapper;
import com.example.inkstonetask.mapper.TaskExecuteLogMapper;
import com.example.inkstonetask.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskQueryService {

    private final TaskMapper taskMapper;
    private final TaskDispatchQueueMapper queueMapper;
    private final TaskExecuteLogMapper executeLogMapper;

    public Task getTaskDetail(Long taskId) {
        return taskMapper.findById(taskId);
    }

    public List<Task> listTasks(int limit) {
        return taskMapper.findAll(limit);
    }

    public List<TaskDispatchQueue> listQueue(int limit) {
        return queueMapper.findAll(limit);
    }

    public List<TaskExecuteLog> listExecuteLogs(int limit) {
        return executeLogMapper.findAll(limit);
    }
}
