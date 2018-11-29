import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualNetwork {

    private Map<Long, Router> networkMap;

    private static final String SWITCH = "Router %s switched %s.";

    private static final String INVALID_SWITCH = "Router %s already %s.";

    private static final String INVALID_ID = "Invalid Router ID : %s.";

    public VirtualNetwork() {
        networkMap = ReadWrite.readNetworkInitFile();
        ReadWrite.print(String.format("Network created with %s routers.", networkMap.size()));
        sendPacket();
    }

    public void shutdownRouter(long routerId) {
        if (!isValidRouterId(routerId)) {
            ReadWrite.printError(String.format(INVALID_ID, routerId));
            return;
        }
        Router router = networkMap.get(routerId);
        if (!router.isActive()) {
            ReadWrite.print(String.format(INVALID_SWITCH, routerId, "off"));
            return;
        }
        router.shutdown();
        ReadWrite.print(String.format(SWITCH, routerId, "off"));
    }

    public void startRouter(long routerId) {
        if (!isValidRouterId(routerId)) {
            ReadWrite.printError(String.format(INVALID_ID, routerId));
            return;
        }
        Router router = networkMap.get(routerId);
        if (router.isActive()) {
            ReadWrite.print(String.format(INVALID_SWITCH, routerId, "on"));
            return;
        }
        router.start();
        ReadWrite.print(String.format(SWITCH, routerId, "on"));
    }

    public void sendPacket() {
        List<LinkStatePacket> lsps = new ArrayList<>();
        for (Router router : networkMap.values()) {
            if (router.isActive()) {
                lsps.add(router.originatePacket());
            }
        }

        for (LinkStatePacket lsp : lsps) {
            while (lsp.isAlive()) {
                Router nextReceiver = lsp.getNextReceiver();
                Long sender = lsp.getSender();
                nextReceiver.receivePacket(sender, lsp);
            }
        }
        ReadWrite.print("Link State Packets sent over the network.");

    }

    private boolean isValidRouterId(long id) {
        return networkMap.containsKey(id);
    }

    public void printRoutingTable(Long routerId) {
        if (isValidRouterId(routerId)) {
            networkMap.get(routerId).displayRoutingTable();
        } else {
            ReadWrite.printError(String.format(INVALID_ID, String.valueOf(routerId)));
        }
    }
}