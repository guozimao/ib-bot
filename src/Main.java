import com.ib.client.Decimal;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        IbClient ib = new IbClient();
        ib.connect();

        MarketDataManager market = new MarketDataManager();
        Strategy strategy = new Strategy(market, ib);

        OrderManager orderManager = new OrderManager(ib);

        // 请求账户数据
        ib.requestAccountData();

        // 拉历史数据
        ib.initHistoricalData(SymbolConfig.CONFIG.keySet().stream().toList());

        // 订阅行情
        SymbolConfig.CONFIG.keySet().forEach(ib::subscribeMarketData);

        RiskManager rm = new RiskManager();

        System.out.println("🚀 Scanner started...");

        // 每秒扫描
        while (true) {

            double accountSize = ib.getAccountEquity();
            rm.setAccountSize(accountSize);

            for (String symbol : SymbolConfig.CONFIG.keySet()) {

                ShouldBuyResult result = strategy.shouldBuy(symbol);
                if (result.isResult()) {

                    orderManager.buy(symbol, result.getDecision(), rm);

                }
            }

            sleep(1000);
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}