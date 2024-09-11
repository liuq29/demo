import java.sql.*;

public class AutoPaginationExample {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";
    
    private static final int PAGE_SIZE = 2000;  // 每页显示2000条

    public static void main(String[] args) {
        fetchAllPages();
    }

    public static void fetchAllPages() {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // 建立数据库连接
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // 1. 获取总记录数
            int totalRecords = getTotalRecords(connection);
            System.out.println("Total Records: " + totalRecords);

            // 2. 计算总页数
            int totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
            System.out.println("Total Pages: " + totalPages);

            // 3. 遍历每一页进行分页查询
            for (int currentPage = 1; currentPage <= totalPages; currentPage++) {
                System.out.println("Fetching page: " + currentPage);
                fetchDataByPage(connection, currentPage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取总记录数
    public static int getTotalRecords(Connection connection) throws SQLException {
        String countQuery = "SELECT COUNT(*) AS total FROM your_table";
        try (PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
            ResultSet resultSet = countStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }
        return 0;
    }

    // 分页查询
    public static void fetchDataByPage(Connection connection, int pageNumber) throws SQLException {
        String query = "SELECT * FROM your_table LIMIT ? OFFSET ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // 计算偏移量
            int offset = (pageNumber - 1) * PAGE_SIZE;

            // 设置查询参数
            statement.setInt(1, PAGE_SIZE);
            statement.setInt(2, offset);

            // 执行查询
            ResultSet resultSet = statement.executeQuery();

            // 处理查询结果
            while (resultSet.next()) {
                // 假设有一个名为 "column_name" 的列
                String data = resultSet.getString("column_name");
                System.out.println(data);
            }
        }
    }
}
