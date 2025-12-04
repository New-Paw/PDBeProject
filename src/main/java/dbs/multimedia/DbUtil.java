package dbs.multimedia;
import java.sql.Connection;
import java.sql.SQLException;
import oracle.jdbc.pool.OracleDataSource;

public class DbUtil {
    private static OracleDataSource dataSource;

    static {
        try {
            dataSource = new OracleDataSource();
            dataSource.setURL("jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb");
            dataSource.setUser("xzhaome00");
            dataSource.setPassword("828PB9GH");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
