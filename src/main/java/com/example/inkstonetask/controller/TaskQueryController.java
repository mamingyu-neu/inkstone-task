package com.example.inkstonetask.controller;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskDispatchQueue;
import com.example.inkstonetask.domain.TaskExecuteLog;
import com.example.inkstonetask.service.TaskQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/task-query")
@RequiredArgsConstructor
public class TaskQueryController {

    private final TaskQueryService taskQueryService;

    @GetMapping("/tasks/{taskId}")
    public Task getTaskDetail(@PathVariable Long taskId) {
        return taskQueryService.getTaskDetail(taskId);
    }

    @GetMapping("/tasks")
    public List<Task> listTasks(@RequestParam(defaultValue = "50") int limit) {
        return taskQueryService.listTasks(limit);
    }

    @GetMapping("/queue")
    public List<TaskDispatchQueue> listQueue(@RequestParam(defaultValue = "50") int limit) {
        return taskQueryService.listQueue(limit);
    }

    @GetMapping("/execute-logs")
    public List<TaskExecuteLog> listExecuteLogs(@RequestParam(defaultValue = "50") int limit) {
        return taskQueryService.listExecuteLogs(limit);
    }
}
