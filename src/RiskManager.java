public class RiskManager {
    // ====== 账户参数 ======
    private final double riskPerTrade = 0.01; // 每笔1%

    public RiskManager() {
    }

    // =========================
    // 1️⃣ ATR止损计算
    // =========================
    public double calcStop(double price, double atr, double k) {
        return price - (atr * k);
    }

    // =========================
    // 2️⃣ 仓位计算（核心）
    // =========================
    public int calcPositionSize(double price, double stopPrice, double accountSize) {

        System.out.println("calcPositionSize "+
                " price=" + price +
                " stopPrice=" + stopPrice +
                " accountSize=" + accountSize);

        double riskPerTradeUSD = accountSize * riskPerTrade;

        double riskPerShare = Math.abs(price - stopPrice);

        if (riskPerShare == 0) return 0;

        //风险限制
        int sizeByRisk = (int)(riskPerTradeUSD / riskPerShare);

        //资金限制
        int sizeByCash =
                (int)(accountSize / price);

        int size = Math.min(
                sizeByRisk,
                sizeByCash
        );

        size = Math.max(0, size); //没有最小/最大仓位限制
        size = Math.min(size, 500);

        if (size <= 0) {
            System.out.println(
                    "仓位计算结果为0 "
                            + " price=" + price
                            + " stop=" + stopPrice
                            + " account=" + accountSize);

        }
        return size;
    }
}
