package com.example.inkstonetask.mapper;

import com.example.inkstonetask.domain.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    int insert(Task task);

    List<Task> findAll(@Param("limit") int limit);

    List<Task> findReadyTasks();

    int markTaskQueued(@Param("taskId") Long taskId);

    Task findById(@Param("taskId") Long taskId);

    int markTaskRunning(@Param("taskId") Long taskId);

    int markTaskQueuedForRetry(@Param("taskId") Long taskId);

    int markTaskFinished(@Param("taskId") Long taskId);

    int markTaskFailed(@Param("taskId") Long taskId);

    int markTaskExpired(@Param("taskId") Long taskId);
}
