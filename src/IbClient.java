import com.ib.client.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IbClient extends EWrapperAdapter{
    private EClientSocket client;

    private final Map<Integer, String> tickerMap = new HashMap<>();

    private int tickerId = 1;

    private final EReaderSignal signal = new EJavaSignal();

    private final Map<String, String> accountData = new ConcurrentHashMap<>();

    private EReader reader;

    private volatile int nextOrderId;

    private volatile boolean idReady = false;

    @Override
    public void tickPrice(int reqId, int field, double price, TickAttrib attrib) {

        //System.out.println("TICK → " + reqId + " field=" + field + " price=" + price);

        if (field == TickType.LAST.index()) {

            String symbol = tickerMap.get(reqId);

            MarketDataManager.onPrice(symbol, price, 0);
        }
    }

    @Override
    public void tickSize(int reqId, int field, Decimal size) {

        if (field == TickType.LAST_SIZE.index()) {

            String symbol = tickerMap.get(reqId);

            double price = MarketDataManager.getPrice(symbol);

            MarketDataManager.onPrice(symbol, price, size.longValue());
        }
    }

    @Override
    public void historicalData(int reqId, Bar bar) {

        String symbol = tickerMap.get(reqId);

        MarketDataManager.onBar(
                symbol,
                bar.high(),
                bar.low(),
                bar.close()
        );
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {

        accountData.put(tag, value);

        System.out.println(
                account + " " + tag + "=" + value
        );
    }

    @Override
    public void nextValidId(int orderId) {
        System.out.println("✅ nextValidId = " + orderId);

        this.nextOrderId = orderId;
        this.idReady = true;
    }

    public void connect() {

        client = new EClientSocket(this, signal);

        client.eConnect("127.0.0.1", 4002, 1);

        // ⭐ 关键1：创建 reader
        reader = new EReader(client, signal);
        reader.start();

        // ⭐ 关键2：启动消息循环线程
        new Thread(() -> {
            while (client.isConnected()) {
                signal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // ⭐ 关键3：主动请求一次（触发 nextValidId）
        client.reqIds(-1);

        while (!idReady) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("✅ IB Ready");
    }

    public void subscribeMarketData(String symbol) {

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.exchange("NASDAQ");   // 或 NYSE
        contract.primaryExch("NASDAQ");
        contract.currency("USD");

        int id = tickerId++;

        tickerMap.put(id, symbol);

        client.reqMarketDataType(MarketDataType.DELAYED_FROZEN.getValue());

        client.reqMktData(id, contract, "", false, false, null);

        System.out.println("📡 Subscribed: " + symbol);
    }

    public EClientSocket client() {
        return client;
    }

    public synchronized int getNextOrderId() {
        if (!idReady) {
            throw new IllegalStateException("OrderId not ready yet");
        }
        return nextOrderId++;
    }

    public void initHistoricalData(List<String> symbols) {

        for (String symbol : symbols) {

            Contract contract = new Contract();
            contract.symbol(symbol);
            contract.secType("STK");
            contract.exchange("SMART");
            contract.currency("USD");

            int reqId = tickerId++;

            tickerMap.put(reqId, symbol);

            client.reqHistoricalData(
                    reqId,
                    contract,
                    "",
                    "2 D",
                    "1 min",
                    "TRADES",
                    1,
                    1,
                    false,
                    null
            );
        }
    }

    @Override
    public void historicalDataEnd(
            int reqId,
            String start,
            String end) {

        String symbol = tickerMap.get(reqId);

        MarketDataManager.initATR(symbol);
    }

    public void requestAccountData() {

        client.reqAccountSummary(
                9001,
                "All",
                "NetLiquidation,EquityWithLoanValue,AvailableFunds"
        );
    }

    public double getAccountEquity() {

        String val = accountData.get("NetLiquidation");

        if (val == null) return 0;

        return Double.parseDouble(val);
    }

    public void disconnect() {

        if (client != null && client.isConnected()) {
            System.out.println("🔌 Disconnecting IB...");
            client.eDisconnect();
        }
    }
}
