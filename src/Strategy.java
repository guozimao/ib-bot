public class Strategy {
    private final MarketDataManager market;
    private final IbClient ib;


    public Strategy(MarketDataManager market, IbClient ib) {
        this.market = market;
        this.ib = ib;
    }

    public ShouldBuyResult shouldBuy(String symbol) {

        double price = market.getPrice(symbol);
        double high = SymbolConfig.CONFIG.getOrDefault(symbol, Double.MAX_VALUE);

        double vwap = MarketDataManager.getVWAP(symbol);
        long volume = MarketDataManager.getVolume(symbol);

        if (price <= 0.0) return new ShouldBuyResult(false, null);

        // 防止重复下单
        if (PositionCache.get(symbol) != null) return new ShouldBuyResult(false, null);

        boolean breakout = price > high * 1.001;
        boolean vwapOk = price > vwap;
        boolean volumeOk = volume > MarketDataManager.getAvgVolume(symbol) * 1.5;
        ExecutionDecision decision = decide(price, vwap, volume, MarketDataManager.getAvgVolume(symbol), breakout);

        if (breakout && (vwapOk || volumeOk)) {

            System.out.println("🔥 BUY " + symbol +
                    " price=" + price +
                    " vwap=" + vwap +
                    " vol=" + volume);
            return new ShouldBuyResult(true, decision);
        }

        return new ShouldBuyResult(false, null);
    }

    public ExecutionDecision decide(double price, double vwap, double volume, double avgVol, boolean breakout) {

        boolean strong = breakout && volume > avgVol * 2.5 && price > vwap;
        boolean normal = breakout && volume > avgVol * 1.5;

        if (strong) {
            return new ExecutionDecision(ExecutionDecision.Type.MARKET, 0, price);
        }

        if (normal) {
            return new ExecutionDecision(
                    ExecutionDecision.Type.LIMIT,
                    price * 1.001,
                    price
            );
        }

        return new ExecutionDecision(ExecutionDecision.Type.SKIP, 0, price);
    }
}
