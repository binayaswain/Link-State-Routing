public class ConnectionInformation {

    private final Router router;

    private final long initCost;

    private long cost;

    private int tick;

    public ConnectionInformation(Router connection, long linkCost) {
        router = connection;
        initCost = linkCost;
        cost = linkCost;
        tick = 0;
    }

    public Router getRouter() {
        return router;
    }

    public int getTick() {
        return tick;
    }

    public void resetTick() {
        tick = 0;
        cost = initCost;
    }

    public void incrementTick(boolean incrementTick) {
        if (incrementTick && ++tick == 2) {
            setCost(Integer.MAX_VALUE);
        }
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long linkCost) {
        cost = linkCost;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("ConnectionInformation [routerID=").append(router.getId()).append(", cost=").append(cost)
                .append(", tick=").append(tick).append("]").append(System.lineSeparator()).toString();
    }

}
