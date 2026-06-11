package com.example.inkstonetask.mapper;

import com.example.inkstonetask.domain.TaskExecuteLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskExecuteLogMapper {
    int insert(TaskExecuteLog log);

    int updateSuccess(TaskExecuteLog log);

    int updateFailed(TaskExecuteLog log);
}
