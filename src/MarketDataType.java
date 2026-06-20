public enum MarketDataType {
    LIVE(1),
    FROZEN(2),
    DELAYED_FROZEN(3),
    DELAYED(4);

    private final int value;

    MarketDataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
