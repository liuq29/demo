1. XXL-JOB 定时任务实现步骤1. 创建 XXL-JOB Handler调整 XXL-JOB Handler，使用分页来分批处理数据。以下是示例代码：import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataMigrationJob {

    @Autowired
    private YourMapper yourMapper; // 注入 MyBatis Mapper，用于数据库操作

    @XxlJob("dataMigrationJobHandler")
    public void execute(String param) throws Exception {
        // 参数解析，如传入的时间参数：2023-07
        String[] yearMonth = param.split("-");
        int year = Integer.parseInt(yearMonth[0]);
        int month = Integer.parseInt(yearMonth[1]);

        int batchSize = 10000; // 每批处理的数据量
        int offset = 0; // 初始偏移量

        while (true) {
            // 分批迁移数据
            int rowsInserted = yourMapper.migrateData(year, month, offset, batchSize);
            if (rowsInserted < batchSize) {
                break; // 如果本批次插入的数据量小于 batchSize，说明数据迁移完成
            }
            offset += batchSize; // 更新偏移量
        }

        // 迁移完成后，开始分批删除
        offset = 0; // 重置偏移量
        while (true) {
            // 分批删除数据
            int rowsDeleted = yourMapper.deleteOldData(year, month, offset, batchSize);
            if (rowsDeleted < batchSize) {
                break; // 如果本批次删除的数据量小于 batchSize，说明数据删除完成
            }
            offset += batchSize; // 更新偏移量
        }
    }
}2. Mapper 接口实现基于分页的 MyBatis Mapper 接口，迁移和删除数据：public interface YourMapper {
    // 分批迁移数据
    @Insert("INSERT INTO A_TEMP (SELECT * FROM A WHERE YEAR(created_at) < #{year} OR (YEAR(created_at) = #{year} AND MONTH(created_at) < #{month}) LIMIT #{batchSize} OFFSET #{offset})")
    int migrateData(int year, int month, int offset, int batchSize);

    // 分批删除数据
    @Delete("DELETE FROM A WHERE YEAR(created_at) < #{year} OR (YEAR(created_at) = #{year} AND MONTH(created_at) < #{month}) LIMIT #{batchSize} OFFSET #{offset}")
    int deleteOldData(int year, int month, int offset, int batchSize);
}2. 在 XXL-JOB 管理控制台配置任务新增任务：任务名称：如 DataMigrationJob任务描述：迁移并删除 A 表中指定时间之前的数据执行器：选择你的执行器任务参数：如 2023-07执行策略：选择合适的执行策略（如失败重试）调度类型：设置为合适的定时策略，如每天凌晨或每周进行迁移定时配置：根据你的需求配置任务的执行频率，避免高峰时段执行。3. 注意事项性能优化：分页查询可能在数据量特别大的情况下效率较低，可以考虑在迁移前对相关表进行索引优化。系统负载：尽量在系统负载较低的时段执行任务，以减少对业务的影响。日志和监控：记录每次任务执行的日志，便于跟踪任务进度和排查问题。4. 性能改进建议如果 LIMIT ... OFFSET 的性能不足，可以考虑使用其他分页策略（如基于 created_at 时间戳的范围分割）。考虑对 created_at 字段创建索引，加快查询速度。通过上述方式，你可以使用 XXL-JOB 稳定地分批迁移和删除大量数据，即使主键是字符串 UUID。
