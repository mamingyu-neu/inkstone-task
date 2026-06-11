CREATE TABLE `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
  `task_type` VARCHAR(50) DEFAULT NULL COMMENT '任务类型',
  `content` TEXT COMMENT '任务内容',
  `status` VARCHAR(32) NOT NULL COMMENT '任务状态：pending/queued/running/finished/failed/expired/cancelled',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
  `plan_start_time` DATETIME DEFAULT NULL COMMENT '计划开始时间',
  `plan_end_time` DATETIME DEFAULT NULL COMMENT '计划结束时间',
  `actual_start_time` DATETIME DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` DATETIME DEFAULT NULL COMMENT '实际结束时间',
  `max_retry_count` INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '当前重试次数',
  `executor_type` VARCHAR(32) DEFAULT NULL COMMENT '执行器类型',
  `executor_config` JSON DEFAULT NULL COMMENT '执行配置',
  `calendar_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否显示到日程表：0否1是',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否1是',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_start_time` (`status`, `plan_start_time`),
  KEY `idx_calendar_time` (`calendar_flag`, `plan_start_time`, `plan_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务主表';

CREATE TABLE `task_dispatch_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `queue_status` VARCHAR(32) NOT NULL COMMENT '队列状态：waiting/running/success/failed/retry_wait',
  `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级',
  `next_trigger_time` DATETIME NOT NULL COMMENT '下次触发时间',
  `worker_id` VARCHAR(64) DEFAULT NULL COMMENT '执行worker',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
  `fail_reason` VARCHAR(1000) DEFAULT NULL COMMENT '失败原因',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_status_trigger` (`queue_status`, `next_trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务调度队列表';

CREATE TABLE `task_execute_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `queue_id` BIGINT DEFAULT NULL COMMENT '队列ID',
  `execute_status` VARCHAR(32) NOT NULL COMMENT '执行状态：running/success/failed/timeout',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `result_msg` VARCHAR(2000) DEFAULT NULL COMMENT '执行结果说明',
  `error_stack` TEXT COMMENT '异常信息',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_queue_id` (`queue_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';

CREATE TABLE `task_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型：create/update/cancel/manual_trigger/retry',
  `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人',
  `operation_desc` VARCHAR(1000) DEFAULT NULL COMMENT '操作说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务操作日志表';
