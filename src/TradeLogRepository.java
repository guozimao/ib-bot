import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TradeLogRepository {
    public void save(TradeLog log) {

        String sql = """
                INSERT INTO trade_log (
                    symbol,
                    entry_time,
                    order_type,
                    entry_price,
                    stop_order_id,
                    stop_price,
                    take_profit_order_id,
                    take_profit_price,
                    atr,
                    quantity,
                    account_size,
                    order_id,
                    status
                )
                VALUES (?, datetime('now','localtime'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection conn = DbManager.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, log.getSymbol());
            ps.setString(2, log.getOrderType());
            ps.setDouble(3, log.getEntryPrice());
            ps.setDouble(4, log.getStopOrderId());
            ps.setDouble(5, log.getStopPrice());
            ps.setDouble(6, log.getTakeProfitOrderId());
            ps.setDouble(7, log.getTakeProfitPrice());
            ps.setDouble(8, log.getAtr());
            ps.setInt(9, log.getQuantity());
            ps.setDouble(10, log.getAccountSize());
            ps.setInt(11, log.getOrderId());
            ps.setString(12, log.getStatus());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeTrade(String symbol, double exitPrice, String reason) {

        TradeLog log = getOpenTrade(symbol);

        if (log == null) return;

        double pnl = (exitPrice - log.getEntryPrice()) * log.getQuantity();

        String sql = """
            UPDATE trade_log
            SET exit_price = ?,
                pnl = ?,
                exit_time = datetime('now','localtime'),
                exit_reason = ?,
                status = 'CLOSED'
            WHERE symbol = ? AND status = 'OPEN'
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, exitPrice);
            ps.setDouble(2, pnl);
            ps.setString(3, reason);
            ps.setString(4, symbol);

            ps.executeUpdate();

            System.out.println("📉 CLOSED: " + symbol + " exit=" + exitPrice + " reason=" + reason);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TradeLog getOpenTrade(String symbol) {

        String sql = """
            SELECT *
            FROM trade_log
            WHERE symbol = ? AND status = 'OPEN'
            ORDER BY id DESC
            LIMIT 1
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, symbol);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                TradeLog log = new TradeLog();

                log.setSymbol(rs.getString("symbol"));

                log.setEntryTime(rs.getString("entry_time"));

                log.setOrderType(rs.getString("order_type"));

                log.setEntryPrice(rs.getDouble("entry_price"));

                log.setStopOrderId(rs.getInt("stop_order_id"));

                log.setStopPrice(rs.getDouble("stop_price"));

                log.setTakeProfitOrderId(rs.getInt("take_profit_order_id"));

                log.setTakeProfitPrice(rs.getDouble("take_profit_price"));

                log.setAtr(rs.getDouble("atr"));

                log.setQuantity(rs.getInt("quantity"));

                log.setAccountSize(rs.getDouble("account_size"));

                log.setOrderId(rs.getInt("order_id"));

                log.setStatus(rs.getString("status"));

                log.setExitPrice(rs.getDouble("exit_price"));

                log.setPnl(rs.getDouble("pnl"));

                log.setExitTime(rs.getString("exit_time"));

                return log;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TradeLog> getAllOpenTrade() {
        List<TradeLog> result = new ArrayList<>();

        String sql = """
            SELECT *
            FROM trade_log
            WHERE status = 'OPEN'
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                TradeLog log = new TradeLog();

                log.setSymbol(rs.getString("symbol"));

                log.setEntryTime(rs.getString("entry_time"));

                log.setOrderType(rs.getString("order_type"));

                log.setEntryPrice(rs.getDouble("entry_price"));

                log.setStopOrderId(rs.getInt("stop_order_id"));

                log.setStopPrice(rs.getDouble("stop_price"));

                log.setTakeProfitOrderId(rs.getInt("take_profit_order_id"));

                log.setTakeProfitPrice(rs.getDouble("take_profit_price"));

                log.setAtr(rs.getDouble("atr"));

                log.setQuantity(rs.getInt("quantity"));

                log.setAccountSize(rs.getDouble("account_size"));

                log.setOrderId(rs.getInt("order_id"));

                log.setStatus(rs.getString("status"));

                log.setExitPrice(rs.getDouble("exit_price"));

                log.setPnl(rs.getDouble("pnl"));

                log.setExitTime(rs.getString("exit_time"));

                result.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void updateStopPrice(String symbol,
                                int stopOrderId,
                                double stopPrice) {

        String sql = """
            UPDATE trade_log
            SET stop_price = ?,
                stop_order_id = ?
            WHERE symbol = ?
              AND status = 'OPEN'
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, stopPrice);
            ps.setInt(2, stopOrderId);
            ps.setString(3, symbol);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ StopPrice Updated: "
                        + symbol + " -> " + stopPrice);
            } else {
                System.out.println("⚠ No OPEN trade found: " + symbol);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
