# inkstone-task

任务调度示例工程。

## 项目说明
这是一个基于 Spring Boot + MyBatis + PostgreSQL 的任务调度示例，实现了“任务创建 -> 定时扫描 -> 入调度队列 -> worker 消费 -> 执行日志记录”的闭环。

## 四张表作用
- `task`：任务主表，保存任务主体、计划开始结束时间、状态和执行配置，并支持日程展示。
- `task_dispatch_queue`：调度执行队列表，保存待执行任务，负责削峰、排队、抢占和重试。
- `task_execute_log`：执行日志表，记录每次执行的状态、时间、结果和异常信息。
- `task_operation_log`：操作日志表，记录任务创建、系统入队、执行成功失败等操作轨迹。

## 调度调用关系流程
1. 调用创建任务接口写入 `task`，初始状态为 `pending`。
2. 若任务带 `plan_start_time`、`plan_end_time`，可用于日程展示。
3. 定时扫描器每 5 分钟扫描 `task` 表中已到执行时间的 `pending` 任务。
4. 扫描成功后将任务状态更新为 `queued`，并插入 `task_dispatch_queue`，状态为 `waiting`。
5. worker 每隔 5 秒从 `task_dispatch_queue` 中拉取 `waiting/retry_wait` 任务并抢占执行。
6. 执行前检查任务是否已超过 `plan_end_time`，若过期则标记 `expired`。
7. 执行成功则更新 `task=finished`、`queue=success`；执行失败则按 `max_retry_count` 进入 `retry_wait` 或最终 `failed`。
8. 每次执行写入 `task_execute_log`，关键动作写入 `task_operation_log`。

## PostgreSQL 初始化与启动
1. 启动 PostgreSQL，并创建数据库：
   ```sql
   CREATE DATABASE inkstone_task;
   ```
2. 在该数据库执行 `src/main/resources/schema.sql`。
3. 根据实际环境修改 `src/main/resources/application.yml` 中 PostgreSQL 连接信息。
4. 启动项目：

```bash
mvn spring-boot:run
```

## 创建任务接口
```http
POST /tasks
Content-Type: application/json
```

请求示例：
```json
{
  "taskName": "demo-task",
  "taskType": "demo",
  "content": "test task",
  "priority": 10,
  "planStartTime": "2026-06-11T10:00:00",
  "planEndTime": "2026-06-11T10:30:00",
  "maxRetryCount": 3,
  "executorType": "default",
  "executorConfig": "{}",
  "operatorName": "admin"
}
```

## 查询接口
- `GET /task-query/tasks/{taskId}`：查询任务详情
- `GET /task-query/tasks?limit=50`：查询任务列表
- `GET /task-query/queue?limit=50`：查询队列列表
- `GET /task-query/execute-logs?limit=50`：查询执行日志列表

## 验证流程
1. 创建一条开始时间早于当前时间、结束时间晚于当前时间的任务。
2. 等待定时扫描器运行，将任务从 `pending` 更新为 `queued` 并写入 `task_dispatch_queue`。
3. 等待 worker 消费队列，任务将进入 `running`，随后更新为 `finished`。
4. 查看：
   - `task`
   - `task_dispatch_queue`
   - `task_execute_log`
   - `task_operation_log`

即可验证完整调度流程。

## 测试
使用 H2 PostgreSQL 兼容模式执行 Spring Boot 集成测试：

```bash
mvn test
```

测试覆盖：
- 创建任务
- 扫描到期任务并入队
- worker 消费成功
- 执行前过期校验
- 执行失败重试与最终失败
