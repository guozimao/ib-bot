import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;


public class OrderManager {
    private final IbClient ib;

    public OrderManager(IbClient ib) {
        this.ib = ib;
    }

    public void buy(String symbol, ExecutionDecision d, RiskManager rm, IbClient ib) {

        if (d.type == ExecutionDecision.Type.SKIP) return;

        double accountSize = ib.getAccountEquity();
        double atr = MarketDataManager.getATR(symbol);
        double stop = Math.max(0, rm.calcStop(d.refPrice, atr, 2.0));;  // RiskManager 计算止损
        Decimal qty = Decimal.get(rm.calcPositionSize(d.refPrice, stop, accountSize));  // 计算仓位

        if (qty.longValue() <= 0) {
            System.out.println(
                    "仓位为0，跳过交易: " + symbol);
            return;
        }

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.exchange("SMART");
        contract.currency("USD");

        int parentId = ib.getNextOrderId();
        int tpId = ib.getNextOrderId();
        int slId = ib.getNextOrderId();

        // =====================
        // Parent（MKT / LMT）
        // =====================
        Order parent = new Order();
        parent.orderId(parentId);
        parent.action("BUY");
        parent.totalQuantity(qty);

        if (d.type == ExecutionDecision.Type.MARKET) {
            parent.orderType("MKT");   // ❗不设置 price
        }

        if (d.type == ExecutionDecision.Type.LIMIT) {
            parent.orderType("LMT");
            parent.lmtPrice(d.limitPrice);
        }

        parent.transmit(false);

        // =====================
        // Take Profit（止盈）
        // =====================
        double tpPrice = d.refPrice * 1.30; // +30%
        Order tp = new Order();
        tp.orderId(tpId);
        tp.action("SELL");
        tp.orderType("LMT");
        tp.lmtPrice(tpPrice);
        tp.totalQuantity(qty);
        tp.parentId(parentId);
        tp.transmit(false);

        // =====================
        // Stop Loss（止损）
        // =====================
        Order sl = new Order();
        sl.orderId(slId);
        sl.action("SELL");
        sl.orderType("STP");
        sl.auxPrice(stop);
        sl.totalQuantity(qty);
        sl.parentId(parentId);
        sl.transmit(true);

        ib.client().placeOrder(parentId, contract, parent);
        ib.client().placeOrder(tpId, contract, tp);
        ib.client().placeOrder(slId, contract, sl);

        saveTradeLog(symbol, parent.getOrderType(), d.refPrice, slId, stop, tpId, tpPrice, atr, qty, accountSize, parentId, "OPEN");

        System.out.println("📦 BRACKET SENT: " + symbol);
    }

    private void saveTradeLog(String symbol, String orderType, double refPrice ,int slId ,double stop, int tpId, double tpPrice, double atr, Decimal qty, double accountSize, int parentId, String status) {
        TradeLog log = new TradeLog();

        log.setSymbol(symbol);

        log.setOrderType(orderType);

        log.setEntryPrice(refPrice);

        log.setStopOrderId(slId);

        log.setStopPrice(stop);

        log.setTakeProfitOrderId(tpId);

        log.setTakeProfitPrice(tpPrice);

        log.setAtr(atr);

        log.setQuantity((int) qty.longValue());

        log.setAccountSize(accountSize);

        log.setOrderId(parentId);

        log.setStatus(status);

        PositionCache.put(symbol, log);

        new TradeLogRepository().save(log);
    }

    public void closeTrade(String symbol, double exitPrice, String reason) {

        new TradeLogRepository().closeTrade(symbol, exitPrice, reason);

    }
}
