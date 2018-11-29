import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Router {

    private final long id;

    private final String networkName;

    private final AtomicLong sequence;

    private AtomicBoolean active;

    private final ConcurrentMap<Long, ConnectionInformation> connectivityGraph;

    private ConcurrentMap<Long, RoutingInformation> routingTable;

    private final ConcurrentMap<Long, Long> sequences;

    public Router(long routerId, String routerName) {
        id = routerId;
        networkName = routerName;
        sequence = new AtomicLong(0);
        active = new AtomicBoolean(true);
        connectivityGraph = new ConcurrentHashMap<>();
        sequences = new ConcurrentHashMap<>();
    }

    public long getId() {
        return id;
    }

    public String getNetworkName() {
        return networkName;
    }

    public boolean isActive() {
        return active.get();
    }

    public void shutdown() {
        active.set(false);
    }

    public void start() {
        active.set(true);
    }

    public void addConnection(Router connection, long cost) {
        connectivityGraph.computeIfAbsent(connection.getId(), k -> new ConnectionInformation(connection, cost));
    }

    public ConcurrentMap<Long, ConnectionInformation> getConnectivityGraph() {
        return connectivityGraph;
    }

    public long getSequence() {
        return sequence.get();
    }

    public long getNextSequence() {
        return sequence.incrementAndGet();
    }

    public void displayRoutingTable() {
        if (routingTable.isEmpty()) {
            computeRoutingTable(null);
        }

        StringBuilder routes = new StringBuilder("Router ").append(id);

        if (isActive()) {
            routes.append(" routing table:").append("%1$s");
        } else {
            routes.append(" is currently inactive.%1$s").append("Last available routing table:").append("%1$s");
        }

        for (RoutingInformation info : routingTable.values()) {
            if (!info.isDeadConnection()) {
                routes.append(info.getNetworkName()).append(ReadWrite.SEPARATOR).append(info.getRouteTo())
                        .append(System.lineSeparator());
            }
        }

        ReadWrite.println(String.format(routes.toString(), System.lineSeparator()));
    }

    public LinkStatePacket originatePacket() {
        LinkStatePacket lsp = new LinkStatePacket(this, 10);
        routingTable = new ConcurrentHashMap<>();
        routePacket(-1L, lsp, true);

        return lsp;
    }

    public void receivePacket(Long senderId, LinkStatePacket lsp) {
        if (!isActive() || !isValidSequence(lsp)) {
            return;
        }

        updateNetwork(senderId, lsp);
        routePacket(senderId, lsp, false);
    }

    private boolean isValidSequence(LinkStatePacket lsp) {
        Long currentSequence = sequences.getOrDefault(lsp.getOriginationRouter(), -1L);

        if (currentSequence >= lsp.getSequence()) {
            return false;
        }

        sequences.put(lsp.getOriginationRouter(), lsp.getSequence());

        return true;
    }

    private void updateNetwork(Long senderId, LinkStatePacket lsp) {
        if (connectivityGraph.containsKey(senderId)) {
            connectivityGraph.get(senderId).resetTick();
        }

        computeRoutingTable(lsp);
    }

    private void computeRoutingTable(LinkStatePacket lsp) {
        initRoutingTable(lsp);

        Queue<RoutingInformation> nodes = new PriorityBlockingQueue<>(routingTable.values());
        Set<Long> visitedNodes = new HashSet<>();

        visitedNodes.add(getId());

        while (!nodes.isEmpty()) {
            RoutingInformation closestRout = nodes.poll();
            if (!visitedNodes.add(closestRout.getRouter().getId())) {
                continue;
            }

            computeCost(closestRout, visitedNodes);
        }
    }

    private void initRoutingTable(LinkStatePacket lsp) {
        if (lsp != null) {
            initRouterConnetivity(lsp.getOriginConnectivity(), false);
        }
        initRouterConnetivity(connectivityGraph.values(), true);
        routingTable.put(getId(), new RoutingInformation(this, 0));
    }

    private void initRouterConnetivity(Collection<ConnectionInformation> routerConnetivity, boolean reuseCost) {
        for (ConnectionInformation info : routerConnetivity) {
            Long cost = reuseCost ? info.getCost() : Integer.MAX_VALUE;
            routingTable.put(info.getRouter().getId(), new RoutingInformation(info.getRouter(), cost));
        }
    }

    private void computeCost(RoutingInformation routingInformation, Set<Long> visitedNodes) {
        Long costToCurrentRouter = routingInformation.getRouteCost();

        for (ConnectionInformation value : routingInformation.getRouter().getConnectivityGraph().values()) {
            Router node = value.getRouter();
            RoutingInformation route = routingTable.get(node.getId());

            if (visitedNodes.contains(node.getId()) || route == null) {
                continue;
            }

            Long combinedCost = costToCurrentRouter + value.getCost();

            if (route.getRouteCost() > combinedCost) {
                route.setRouteCost(combinedCost);
                route.setRouteTo(routingInformation.getRouteTo());
            }
        }
    }

    private void routePacket(Long senderId, LinkStatePacket lsp, boolean incrementTick) {
        lsp.decreaseTimeToLive();

        for (ConnectionInformation info : connectivityGraph.values()) {
            if (info.getRouter().getId() != senderId) {
                info.incrementTick(incrementTick);
                lsp.addReceiver(info.getRouter());
                lsp.addSender(id);
            }
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Router other = (Router) obj;

        return Objects.equals(getId(), other.getId());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append("Router [networkName=").append(networkName).append(", id=").append(id).append(", active=")
                .append(active).append("]").append(System.lineSeparator()).toString();
    }

}
