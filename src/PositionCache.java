import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PositionCache {
    private static final Map<String, TradeLog> cache = new ConcurrentHashMap<>();

    // 放入持仓
    public static void put(String symbol, TradeLog log) {
        cache.put(symbol, log);
    }

    // 获取持仓
    public static TradeLog get(String symbol) {
        return cache.get(symbol);
    }

    // 删除持仓（平仓后调用）
    public static void remove(String symbol) {
        cache.remove(symbol);
    }

    // 是否持仓
    public static boolean exists(String symbol) {
        return cache.containsKey(symbol);
    }

    // 获取全部持仓
    public static Map<String, TradeLog> all() {
        return cache;
    }

}
