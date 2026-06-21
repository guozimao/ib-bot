public class TradeLog {

    /**
     * 股票代码
     * 例如：AAPL、NVDA、MU
     *
     * */
    private String symbol;

    /**
     * 建仓时间
     * 格式：2026-06-21T09:35:12
     *
     * */
    private String entryTime;

    /**
     * 下单类型
     * MARKET = 市价突破
     * LIMIT  = 限价突破
     *
     * */
    private String orderType;

    /**
     * 入场参考价格
     * MARKET: 触发时价格
     * LIMIT : 挂单价格
     *
     * */
    private double entryPrice;

    /**
     * ATR止损价
     *
     * */
    private double stopPrice;

    /**
     * 止盈价
     *
     * */
    private double takeProfitPrice;

    /**
     * 下单时ATR值
     *
     * */
    private double atr;

    /**
     * 买入股数
     *
     * */
    private int quantity;

    /**
     * 下单时账户净值
     *
     * */
    private double accountSize;

    /**
     * IB Parent Order ID
     *
     * */
    private int orderId;

    /**
     * 订单状态
     * OPEN   已成交
     * CLOSED    已平仓
     *
     * */
    private String status;

    /**
     * 平仓价格（Exit Price）
     * 交易结束时的成交价格（止盈/止损触发时的市场价格）
     *
     * */
    private double exitPrice;

    /**
     * 盈亏（PnL, Profit and Loss）
     * 该笔交易的最终收益/亏损金额
     * 计算方式：(exit_price - entry_price) * quantity
     *
     * */
    private double pnl;

    /**
     * 平仓时间（Exit Time）
     * 该笔交易结束的时间（止盈/止损/手动平仓）
     * 示例：2026-06-21 15:30:05
     *
     * */
    private String exitTime;

    /**
     * 平仓理由
     *
     * */
    private String exitReason;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public double getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(double entryPrice) {
        this.entryPrice = entryPrice;
    }

    public double getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(double stopPrice) {
        this.stopPrice = stopPrice;
    }

    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public void setTakeProfitPrice(double takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    public double getAtr() {
        return atr;
    }

    public void setAtr(double atr) {
        this.atr = atr;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAccountSize() {
        return accountSize;
    }

    public void setAccountSize(double accountSize) {
        this.accountSize = accountSize;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public double getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(double exitPrice) {
        this.exitPrice = exitPrice;
    }

    public double getPnl() {
        return pnl;
    }

    public void setPnl(double pnl) {
        this.pnl = pnl;
    }

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }
}
