public class RiskManager {
    // ====== 账户参数 ======
    private volatile double accountSize;
    private final double riskPerTrade = 0.01; // 每笔1%

    public RiskManager() {
    }

    public double getAccountSize() {
        return accountSize;
    }

    public void setAccountSize(double accountSize) {
        if (accountSize > 0) {
            this.accountSize = accountSize;
        }
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
    public int calcPositionSize(double price, double stopPrice) {

        double riskPerTradeUSD = accountSize * riskPerTrade;

        double riskPerShare = Math.abs(price - stopPrice);

        if (riskPerShare == 0) return 0;

        int size = (int)(riskPerTradeUSD / riskPerShare);
        size = Math.max(1, size); //没有最小/最大仓位限制
        size = Math.min(size, 500);
        return size;
    }
}
