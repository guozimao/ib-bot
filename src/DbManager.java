import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {
    private static final String URL =
            "jdbc:sqlite:data/trade.db";

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(URL);
    }

    public static void init() {

        String sql = """
                CREATE TABLE IF NOT EXISTS trade_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,

                    symbol TEXT NOT NULL,

                    entry_time TEXT NOT NULL,

                    order_type TEXT NOT NULL,

                    entry_price REAL NOT NULL,

                    stop_price REAL NOT NULL,

                    take_profit_price REAL NOT NULL,

                    atr REAL NOT NULL,

                    quantity INTEGER NOT NULL,

                    account_size REAL NOT NULL,

                    order_id INTEGER NOT NULL,

                    status TEXT NOT NULL,

                    exit_price REAL NULL,

                    pnl REAL NULL,

                    exit_time TEXT NULL,

                    exit_reason TEXT NULL
                )
                """;

        try (
                Connection conn = getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute(sql);

            System.out.println("✅ trade_log ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
