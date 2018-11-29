import java.io.IOException;
import java.util.Scanner;

public class Application {

    public static void main(String[] args) throws IOException {
        VirtualNetwork virtualNetwork = new VirtualNetwork();
        String input;

        try (Scanner sc = new Scanner(System.in)) {
            while (!"Q".equalsIgnoreCase(input = ReadWrite.getUserInput(sc))) {
                String[] commands = input.split("\\s+");

                switch (commands[0].toUpperCase()) {
                    case "C":
                        virtualNetwork.sendPacket();
                        break;
                    case "S":
                        virtualNetwork.shutdownRouter(Long.parseLong(commands[1]));
                        break;
                    case "T":
                        virtualNetwork.startRouter(Long.parseLong(commands[1]));
                        break;
                    case "P":
                        virtualNetwork.printRoutingTable(Long.parseLong(commands[1]));
                        break;
                    default:
                        break;
                }
            }
        }

        ReadWrite.print("Network disconneted.");

    }
}