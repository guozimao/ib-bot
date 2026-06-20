public class ExecutionDecision {
    public enum Type {
        MARKET,
        LIMIT,
        SKIP
    }

    public final Type type;
    public final double limitPrice;
    public final double refPrice;

    public ExecutionDecision(Type type, double limitPrice, double refPrice) {
        this.type = type;
        this.limitPrice = limitPrice;
        this.refPrice = refPrice;
    }
}
