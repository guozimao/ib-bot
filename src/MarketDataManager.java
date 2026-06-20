import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataManager {

    static class State {
        double price;

        double cumPV;
        long cumVol;

        long lastSize;

        long volumeWindow;
        long volumeCount;
        double avgVol;
    }

    static class Bar {
        double high;
        double low;
        double close;

        Bar(double h, double l, double c) {
            this.high = h;
            this.low = l;
            this.close = c;
        }
    }

    private static final Map<String, State> data = new ConcurrentHashMap<>();

    private static final Map<String, List<Bar>> bars = new ConcurrentHashMap<>();

    private static final Map<String, Double> atrMap = new ConcurrentHashMap<>();

    private static final int ATR_PERIOD = 30;

    public static void onPrice(String symbol, double price, long size) {

        State s = data.computeIfAbsent(symbol, k -> new State());

        s.price = price;

        // VWAP
        s.cumPV += price * size;
        s.cumVol += size;

        // volume
        s.lastSize = size;

        // rolling average（简单版）
        if (size > 0) {
            s.volumeWindow += size;
            s.volumeCount++;

            s.avgVol = (double) s.volumeWindow / s.volumeCount;
        }
    }

    public static double getPrice(String symbol) {
        return data.getOrDefault(symbol, new State()).price;
    }

    public static double getVWAP(String symbol) {
        State s = data.get(symbol);
        if (s == null || s.cumVol == 0) return 0;
        return s.cumPV / s.cumVol;
    }

    public static long getVolume(String symbol) {
        State s = data.get(symbol);
        return s == null ? 0 : s.cumVol;
    }

    public static double getAvgVolume(String symbol) {
        State s = data.get(symbol);
        return s == null ? 0 : s.avgVol;
    }

    public static void onBar(String symbol, double high, double low, double close) {

        bars.computeIfAbsent(symbol, k -> new ArrayList<>())
                .add(new Bar(high, low, close));

    }

    public static double getATR(String symbol) {
        return atrMap.getOrDefault(symbol, 0.0);
    }

    public static void initATR(String symbol) {

        List<Bar> list = bars.get(symbol);
        System.out.println(
                symbol + " list=" + bars.get(symbol)
        );

        if (list == null || list.size() < ATR_PERIOD + 1) {
            return ;
        }

        double sumTR = 0;

        for (int i = 1; i < ATR_PERIOD + 1; i++) {

            Bar curr = list.get(list.size() - i);
            Bar prev = list.get(list.size() - i - 1);

            double tr = Math.max(
                    curr.high - curr.low,
                    Math.max(
                            Math.abs(curr.high - prev.close),
                            Math.abs(curr.low - prev.close)
                    )
            );

            sumTR += tr;
        }

        atrMap.put(symbol,sumTR / ATR_PERIOD);

        bars.remove(symbol);
    }
}
