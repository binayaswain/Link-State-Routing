import java.util.Objects;

public class RoutingInformation implements Comparable<RoutingInformation> {

    private final Router router;

    private long routeCost;

    private long routeTo;

    public RoutingInformation(Router connection, long routingCost) {
        router = connection;
        routeTo = connection.getId();
        routeCost = routingCost;
    }

    public Router getRouter() {
        return router;
    }

    public long getRouteTo() {
        return routeTo;
    }

    public void setRouteTo(long route) {
        routeTo = route;
    }

    public long getRouteCost() {
        return routeCost;
    }

    public void setRouteCost(long cost) {
        routeCost = cost;
    }

    public String getNetworkName() {
        return router.getNetworkName();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(router.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        return Objects.equals(getNetworkName(), ((RoutingInformation) obj).getNetworkName());
    }

    @Override
    public int compareTo(RoutingInformation o) {
        int diff = Long.compare(getRouteCost(), o.getRouteCost());
        return diff == 0 ? -1 : diff;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("RoutingInformation [routerID=").append(router.getId()).append(", routeTo=")
                .append(routeTo).append(", routeCost=").append(routeCost).append("]").append(System.lineSeparator())
                .toString();
    }

    public boolean isDeadConnection() {
        return routeCost >= Integer.MAX_VALUE && routeTo == router.getId();
    }
}
