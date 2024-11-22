package com.example.migration.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataMigrationJob {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 10000;

    @XxlJob("dataMigrationHandler")
    public void executeMigration() {
        String startDate = "2024-10-01";
        String endDate = "2024-10-31";

        try {
            boolean hasMoreData;
            do {
                hasMoreData = migrateBatch(startDate, endDate);
            } while (hasMoreData);

            System.out.println("数据迁移完成！");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("数据迁移失败: " + e.getMessage());
        }
    }

    private boolean migrateBatch(String startDate, String endDate) {
        // 获取字段列表
        List<String> columns = getColumns("A");
        String columnList = String.join(",", columns);

        // 查询数据
        String selectQuery = "SELECT " + columnList + " FROM A WHERE create_date >= ? AND create_date <= ? LIMIT ?";
        List<Object[]> rows = jdbcTemplate.query(selectQuery, new Object[]{startDate, endDate, BATCH_SIZE}, (rs, rowNum) -> {
            Object[] row = new Object[columns.size()];
            for (int i = 0; i < columns.size(); i++) {
                row[i] = rs.getObject(columns.get(i));
            }
            return row;
        });

        if (rows.isEmpty()) {
            return false;
        }

        // 插入 B 表
        String insertQuery = "INSERT INTO B (" + columnList + ") VALUES (" + "?,".repeat(columns.size() - 1) + "?)";
        jdbcTemplate.batchUpdate(insertQuery, rows);

        // 删除 A 表数据
        List<Long> ids = rows.stream().map(row -> (Long) row[0]).toList(); // 假设第一列是主键 id
        String deleteQuery = "DELETE FROM A WHERE id IN (" + "?,".repeat(ids.size() - 1) + "?)";
        jdbcTemplate.update(deleteQuery, ids.toArray());

        return true;
    }

    private List<String> getColumns(String tableName) {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'your_database' AND TABLE_NAME = ?";
        return jdbcTemplate.query(query, new Object[]{tableName}, (rs, rowNum) -> rs.getString("COLUMN_NAME"));
    }
}
