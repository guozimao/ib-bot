import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        DbManager.init();

        TradeLogRepository tradeLogRepository = new TradeLogRepository();

        SymbolConfig.CONFIG.keySet().forEach(symbol -> {
            TradeLog log = tradeLogRepository.getOpenTrade(symbol);
            if (log != null){
                PositionCache.put(symbol, log);
            }
        });

        IbClient ib = new IbClient();
        ib.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println("🛑 Program stopping...");
                    ib.disconnect();
                })
        );

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

        // 手动平仓线程
        new Thread(() -> {

            Scanner sc = new Scanner(System.in);

            while (true) {

                System.out.println("📌 输入手动平仓: symbol price");

                String symbol = sc.next();
                double price = sc.nextDouble();

                orderManager.closeTrade(symbol, price, "MANUAL");
                TradeLog log = PositionCache.get(symbol);
                PositionCache.remove(symbol);
                log.setStatus("CLOSED");
                PositionCache.put(symbol, log);

                System.out.println("✅ MANUAL CLOSED: " + symbol);
            }

        }).start();
        System.out.println("🚀 Scanner started...");

        // 每秒扫描
        while (true) {

            for (String symbol : SymbolConfig.CONFIG.keySet()) {

                // =========================
                // ① 买入逻辑
                // =========================
                ShouldBuyResult result = strategy.shouldBuy(symbol);
                if (result.isResult()) {

                    orderManager.buy(symbol, result.getDecision(), rm, ib);

                }

                // =========================
                // ② 卖出逻辑（关键）
                // =========================
                TradeLog log = PositionCache.get(symbol);

                if (log != null && "OPEN".equals(log.getStatus())) {

                    double price = MarketDataManager.getPrice(symbol);

                    // ❗过滤无效行情
                    if (price <= 0) {

                        sleep(1000);

                        continue;
                    }

                    if (price >= log.getTakeProfitPrice()) {
                        orderManager.closeTrade(symbol, price, "TP");
                        PositionCache.remove(symbol);
                        log.setStatus("CLOSED");
                        PositionCache.put(symbol, log);
                    }

                    if (price <= log.getStopPrice()) {
                        orderManager.closeTrade(symbol, price, "SL");
                        PositionCache.remove(symbol);
                        log.setStatus("CLOSED");
                        PositionCache.put(symbol, log);
                    }
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