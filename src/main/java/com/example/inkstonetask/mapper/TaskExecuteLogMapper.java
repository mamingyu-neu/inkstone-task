package com.example.inkstonetask.mapper;

import com.example.inkstonetask.domain.TaskExecuteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskExecuteLogMapper {
    int insert(TaskExecuteLog log);

    List<TaskExecuteLog> findAll(@Param("limit") int limit);

    int updateSuccess(TaskExecuteLog log);

    int updateFailed(TaskExecuteLog log);
}
