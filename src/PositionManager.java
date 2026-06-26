import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.Order;

public class PositionManager {
    private final IbClient ib;

    public PositionManager(IbClient ib){
        this.ib = ib;
    }

    public void updateStops(String symbol){

        updateStop(PositionCache.get(symbol));

    }

    private void updateStop(TradeLog log){

        String symbol = log.getSymbol();

        double close = MarketDataManager.getClose(symbol);

        double atr = MarketDataManager.getATR(symbol);

        RiskManager rm = new RiskManager();

        double low10 = MarketDataManager.getLowestLow(symbol, 10);

        double candidateStop = Math.max(
                low10,
                rm.calcStop(close, atr, 2.5)
        );

        if(candidateStop <= log.getStopPrice()){

            return;

        }

        modifyStop(log, candidateStop);

    }

    private void modifyStop(TradeLog log,double stop){

        Contract contract = new Contract();

        contract.symbol(log.getSymbol());
        contract.secType("STK");
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();

        order.orderId(log.getStopOrderId());

        order.action("SELL");

        order.orderType("STP");

        order.totalQuantity(Decimal.get(log.getQuantity()));

        order.auxPrice(stop);

        // ib请求更改订单
        ib.modifyOrder(contract,order);

        log.setStopPrice(stop);

        // 更新缓存
        PositionCache.remove(log.getSymbol());
        PositionCache.put(log.getSymbol(), log);

        // 更新数据库
        TradeLogRepository tradeLogRepository = new TradeLogRepository();
        tradeLogRepository.updateStopPrice(log.getSymbol(), log.getStopOrderId(), log.getStopPrice());

        System.out.println(
                log.getSymbol()
                        + " Stop更新为:"
                        + stop
        );

    }
}
