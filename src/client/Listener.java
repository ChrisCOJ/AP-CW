package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;


class Listener implements Runnable {
    /* This thread handles every server message sent to a client */

    private final BufferedReader in;
    private final PrintWriter out;
    private final Client client;

    public Listener(Client client, BufferedReader in, PrintWriter out) {
        this.client = client;
        this.in = in;
        this.out = out;
    }

    private void handleServerRequest(String serverMessage) throws RuntimeException {
        // Split the message into 2 parts (separator = space)
        // The first 'word' of the message always represents the type of instruction to be preformed
        // The second 'word' represents the payload / message
        String[] msgParts = serverMessage.split(" ", 2);
        switch (msgParts[0]) {
            case ("text"):  // Send normal text message
                System.out.print("\033[2K");  // Clear user message prompt (i.e. 'Chris: ')
                System.out.print("\r");  // Move cursor to the beginning of the line
                System.out.println(msgParts[1]);  // Print new message
                System.out.print(client.userID + ": ");  // Display message prompt again
                break;
            case ("memberList"):  // Member list sent to the coordinator client
                client.memberList = msgParts[1].split(";;");
                break;
            case ("activateCoordinator"):  // Set coordinator flag to true on the client-side
                client.isCoordinator = true;
                break;
            case ("requestCoordinatorMemberList"):  // Server requests the member list from the coordinator client
                if (client.isCoordinator) {
                    // msgParts[1] = userID that requested the member list
                    out.println("sendCoordinatorMemberList " + msgParts[1] + " " + Arrays.toString(client.memberList));
                    out.flush();
                }
                break;
            case ("sendCoordinatorMemberList"):  // Server message containing the member list in string form
                // Client processes the list in string form and prints it
                System.out.println("\n*** Members List ***");
                for (String line : msgParts[1].split(", ")) {
                    System.out.println(line);
                }
                System.out.print("\n");
                client.receivedList = true;
                break;
            case ("exit"):
                client.stopThreads = true;
                throw new RuntimeException(msgParts[1]);
        }
    }

    public void run() {
        try {
            String serverMessage;  // This variable stores a message sent by the server
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