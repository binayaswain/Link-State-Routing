import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class LinkStatePacket {

    private final long originationRouter;

    private final long sequence;

    private final Set<ConnectionInformation> originConnectivity;

    private final Deque<Router> receivers;

    private final Deque<Long> senders;

    private int timeToLive;

    public LinkStatePacket(Router originRouter, int aliveTime) {
        originationRouter = originRouter.getId();
        sequence = originRouter.getNextSequence();
        timeToLive = aliveTime;
        originConnectivity = new HashSet<>(originRouter.getConnectivityGraph().values());
        receivers = new LinkedList<>();
        senders = new LinkedList<>();
        initSenderAndReceiver();
    }

    private void initSenderAndReceiver() {
        for (ConnectionInformation connection : originConnectivity) {
            receivers.add(connection.getRouter());
            senders.add(getOriginationRouter());
        }
    }

    public Long getOriginationRouter() {
        return originationRouter;
    }

    public long getSequence() {
        return sequence;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void decreaseTimeToLive() {
        timeToLive--;
    }

    public void addReceiver(Router router) {
        receivers.add(router);
    }

    public void addSender(Long sender) {
        senders.add(sender);
    }

    public Router getNextReceiver() {
        return receivers.remove();
    }

    public Long getSender() {
        return senders.remove();
    }

    public Set<ConnectionInformation> getOriginConnectivity() {
        return originConnectivity;
    }

    public boolean isAlive() {
        return timeToLive > 0 && !receivers.isEmpty() && !senders.isEmpty() && senders.size() == receivers.size();
    }
}
