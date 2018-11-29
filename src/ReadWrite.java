import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadWrite {

    private static final Logger LOG = Logger.getLogger(ReadWrite.class.getCanonicalName());

    private static final String MESSAGE = String.format("C = Continue%1$sQ = Quit%1$sS ID = Shut Down Router ID"
            + "%1$sT ID = Start Up Router ID%1$sP ID = Print Routing Table of Router ID%1$s%1$sEnter your choice : ",
            System.lineSeparator());

    public static final String SEPARATOR = ", ";

    private ReadWrite() {

    }

    public static String getUserInput(Scanner sc) {
        String choice = null;

        while (choice == null) {
            println(MESSAGE);
            choice = sc.nextLine();
            if (choice == null || !choice.matches("(?i)c|s \\d+|t \\d+|p \\d+|q")) {
                printError("Invalid input.");
                choice = null;
            }
        }

        return choice;
    }

    public static Map<Long, Router> readNetworkInitFile() {
        Map<Long, Router> network = new HashMap<>();

        try (FileReader fileReader = new FileReader(new File("infile.dat"));
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            Map<Long, List<String>> nodeConnections = new HashMap<>();
            String line;
            Long currentRouterId = -1L;

            while ((line = bufferedReader.readLine()) != null) {
                currentRouterId = createRouters(line, nodeConnections, currentRouterId, network);
            }

            processConnections(nodeConnections, network);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error while reading input file.", e);
            printError(String.format("Error while reading input file : %s", e.getMessage()));
            System.exit(1);
        }

        return network;
    }

    private static Long createRouters(String line, Map<Long, List<String>> nodeConnections, Long currentRouterId,
            Map<Long, Router> network) throws IOException {
        line = line.trim();

        if (line.indexOf('.') == -1) {
            List<String> connections = nodeConnections.getOrDefault(currentRouterId, new ArrayList<String>());
            connections.add(line);
            nodeConnections.put(currentRouterId, connections);
            return currentRouterId;
        }

        String[] routerInfo = line.split("\\s+");

        if (routerInfo.length != 2) {
            throw new IOException(String.format("Invalid router information : %s", line));
        }

        Long routerId = Long.parseLong(routerInfo[0].trim());
        network.put(routerId, new Router(routerId, routerInfo[1].trim()));

        return routerId;
    }

    private static void processConnections(Map<Long, List<String>> connections, Map<Long, Router> network)
            throws IOException {
        for (Entry<Long, List<String>> entry : connections.entrySet()) {
            Router node = network.get(entry.getKey());

            for (String connection : entry.getValue()) {
                connection = connection.trim();
                String[] connectionInfo = connection.split("\\s+");
                if (connectionInfo.length == 0 || connectionInfo.length > 2) {
                    throw new IOException(String.format("Invalid connection information : %s", connection));
                }

                Long connectedNodeId = Long.parseLong(connectionInfo[0]);
                Router connectedNode = network.get(connectedNodeId);
                long cost = connectionInfo.length == 2 ? Long.parseLong(connectionInfo[1]) : 1;

                connectedNode.addConnection(node, cost);
                node.addConnection(connectedNode, cost);
            }
        }
    }

    public static void print(String message) {
        System.out.println();
        System.out.println(message);
    }

    public static void println(String message) {
        System.out.println();
        System.out.print(message);
    }

    public static void printError(String message) {
        System.out.println();
        System.err.println(message);
    }

}
