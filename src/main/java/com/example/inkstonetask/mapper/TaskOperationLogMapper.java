package com.example.inkstonetask.mapper;

import com.example.inkstonetask.domain.TaskOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskOperationLogMapper {
    int insert(TaskOperationLog log);
}
