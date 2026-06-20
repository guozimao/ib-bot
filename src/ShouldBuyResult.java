public class ShouldBuyResult {
    private boolean result;
    private ExecutionDecision decision;

    public ShouldBuyResult(boolean result, ExecutionDecision decision) {
        this.result = result;
        this.decision = decision;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public ExecutionDecision getDecision() {
        return decision;
    }

    public void setDecision(ExecutionDecision decision) {
        this.decision = decision;
    }
}
