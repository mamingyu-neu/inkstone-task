package com.example.inkstonetask.controller;

import com.example.inkstonetask.dto.TaskCreateRequest;
import com.example.inkstonetask.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public Map<String, Object> createTask(@RequestBody TaskCreateRequest request) {
        Long taskId = taskService.createTask(request);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("taskId", taskId);
        return result;
    }
}
