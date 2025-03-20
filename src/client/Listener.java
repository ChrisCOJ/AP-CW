package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


class Listener implements Runnable {
    private final BufferedReader in;
    private final PrintWriter out;
    private final Client client;

    public Listener(Client client, BufferedReader in, PrintWriter out) {
        this.client = client;
        this.in = in;
        this.out = out;
    }

    private void handleServerRequest(String serverMessage) throws RuntimeException {
        String[] msgParts = serverMessage.split(" ", 2);
        switch (msgParts[0]) {
            case ("text"):
                System.out.println("\n" + msgParts[1]); // Print new message
                System.out.print("> "); // Show prompt again
                break;
            case ("memberList"):
                client.memberList = msgParts[1].split(";;");
                break;
            case ("activateCoordinator"):
                client.isCoordinator = true;
                break;
            case ("requestCoordinatorMemberList"):
                if (client.isCoordinator) {
                    // msgParts[1] = userID associated with the request
                    out.println(
                            "sendCoordinatorMemberList " + msgParts[1] +
                                    " " + Arrays.toString(client.memberList)
                    );
                    out.flush();
                }
                break;
            case ("sendCoordinatorMemberList"):
                System.out.println("*** Members List ***");
                for (String line : msgParts[1].split(", ")) {
                    System.out.println(line);
                }
                break;
            case ("exit"):
                client.stopThreads = true;
                throw new RuntimeException(msgParts[1]);
        }
    }

    public void run() {
        try {
            String serverMessage;
            while (!client.stopThreads && (serverMessage = in.readLine()) != null) {
                try {
                    handleServerRequest(serverMessage);
                } catch (RuntimeException e) {
                    System.err.println(e.getMessage() + ", Disconnecting...");
                }
            }
        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
}