package com.example.inkstonetask.mapper;

import com.example.inkstonetask.domain.TaskDispatchQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskDispatchQueueMapper {
    int insertQueue(TaskDispatchQueue queue);

    List<TaskDispatchQueue> findAll(@Param("limit") int limit);

    TaskDispatchQueue findById(@Param("queueId") Long queueId);

    List<TaskDispatchQueue> findExecutableQueueItems(@Param("now") LocalDateTime now, @Param("limit") int limit);

    int claimQueueItem(@Param("queueId") Long queueId, @Param("workerId") String workerId);

    int markQueueSuccess(@Param("queueId") Long queueId);

    int markQueueFailed(@Param("queueId") Long queueId, @Param("failReason") String failReason);

    int markQueueRetry(@Param("queueId") Long queueId,
                       @Param("nextTriggerTime") LocalDateTime nextTriggerTime,
                       @Param("failReason") String failReason);
}
