package com.example.inkstonetask;

import com.example.inkstonetask.domain.Task;
import com.example.inkstonetask.domain.TaskDispatchQueue;
import com.example.inkstonetask.domain.TaskExecuteLog;
import com.example.inkstonetask.dto.TaskCreateRequest;
import com.example.inkstonetask.mapper.TaskDispatchQueueMapper;
import com.example.inkstonetask.mapper.TaskExecuteLogMapper;
import com.example.inkstonetask.mapper.TaskMapper;
import com.example.inkstonetask.scheduler.TaskScanScheduler;
import com.example.inkstonetask.scheduler.TaskWorker;
import com.example.inkstonetask.service.TaskExecutorService;
import com.example.inkstonetask.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class TaskFlowIntegrationTest {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskScanScheduler taskScanScheduler;
    @Autowired
    private TaskWorker taskWorker;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private TaskDispatchQueueMapper queueMapper;
    @Autowired
    private TaskExecuteLogMapper executeLogMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private TaskExecutorService taskExecutorService;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM task_execute_log");
        jdbcTemplate.update("DELETE FROM task_operation_log");
        jdbcTemplate.update("DELETE FROM task_dispatch_queue");
        jdbcTemplate.update("DELETE FROM task");
        Mockito.reset(taskExecutorService);
    }

    @Test
    void shouldCreateTask() {
        Long taskId = taskService.createTask(buildRequest(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(30), 2));
        Task saved = taskMapper.findById(taskId);
        assertNotNull(saved);
        assertEquals("pending", saved.getStatus());
    }

    @Test
    void shouldScanReadyTaskAndEnqueue() {
        Long taskId = taskService.createTask(buildRequest(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(30), 2));

        taskScanScheduler.scanTasks();

        Task task = taskMapper.findById(taskId);
        List<TaskDispatchQueue> queueList = queueMapper.findAll(10);
        assertEquals("queued", task.getStatus());
        assertEquals(1, queueList.size());
        assertEquals("waiting", queueList.get(0).getQueueStatus());
    }

    @Test
    void shouldConsumeQueueSuccessfully() {
        Long taskId = taskService.createTask(buildRequest(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(30), 2));
        taskScanScheduler.scanTasks();
        TaskDispatchQueue queue = queueMapper.findAll(1).get(0);

        taskWorker.process(queue);

        Task task = taskMapper.findById(taskId);
        TaskDispatchQueue dbQueue = queueMapper.findById(queue.getId());
        TaskExecuteLog executeLog = executeLogMapper.findAll(1).get(0);
        assertEquals("finished", task.getStatus());
        assertEquals("success", dbQueue.getQueueStatus());
        assertEquals("success", executeLog.getExecuteStatus());
    }

    @Test
    void shouldMarkTaskExpiredBeforeExecution() {
        Long taskId = taskService.createTask(buildRequest(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(30), 2));
        taskScanScheduler.scanTasks();
        TaskDispatchQueue queue = queueMapper.findAll(1).get(0);
        jdbcTemplate.update("UPDATE task SET plan_end_time = ? WHERE id = ?", LocalDateTime.now().minusSeconds(1), taskId);

        taskWorker.process(queue);

        Task task = taskMapper.findById(taskId);
        TaskDispatchQueue dbQueue = queueMapper.findById(queue.getId());
        TaskExecuteLog executeLog = executeLogMapper.findAll(1).get(0);
        assertEquals("expired", task.getStatus());
        assertEquals("failed", dbQueue.getQueueStatus());
        assertEquals("failed", executeLog.getExecuteStatus());
        assertEquals("task expired", executeLog.getResultMsg());
    }

    @Test
    void shouldRetryThenFailWhenReachMaxRetry() {
        Long taskId = taskService.createTask(buildRequest(LocalDateTime.now().minusMinutes(1), LocalDateTime.now().plusMinutes(30), 1));
        taskScanScheduler.scanTasks();
        TaskDispatchQueue firstQueue = queueMapper.findAll(1).get(0);
        doThrow(new RuntimeException("mock-failed")).when(taskExecutorService).execute(any(Task.class));

        taskWorker.process(firstQueue);

        TaskDispatchQueue retryQueue = queueMapper.findById(firstQueue.getId());
        Task taskAfterFirstFail = taskMapper.findById(taskId);
        assertEquals("retry_wait", retryQueue.getQueueStatus());
        assertEquals(1, retryQueue.getRetryCount());
        assertEquals("queued", taskAfterFirstFail.getStatus());

        taskWorker.process(retryQueue);

        TaskDispatchQueue failedQueue = queueMapper.findById(firstQueue.getId());
        Task failedTask = taskMapper.findById(taskId);
        List<TaskExecuteLog> logs = executeLogMapper.findAll(10);
        assertEquals("failed", failedQueue.getQueueStatus());
        assertEquals("failed", failedTask.getStatus());
        assertEquals(2, logs.size());
    }

    private TaskCreateRequest buildRequest(LocalDateTime planStartTime, LocalDateTime planEndTime, int maxRetryCount) {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTaskName("test-task");
        request.setTaskType("test");
        request.setContent("payload");
        request.setPriority(10);
        request.setPlanStartTime(planStartTime);
        request.setPlanEndTime(planEndTime);
        request.setMaxRetryCount(maxRetryCount);
        request.setExecutorType("default");
        request.setExecutorConfig("{\"k\":\"v\"}");
        request.setOperatorName("tester");
        return request;
    }
}
