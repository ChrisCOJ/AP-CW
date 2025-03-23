package server;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable {

    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientID;
    private boolean isCoordinator = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    public void setCoordinator(boolean status) {
        isCoordinator = status;
        sendMessage("text", "*** You are now the coordinator ***");
        sendMessage("activateCoordinator", "");
    }


    public String getClientID() {
        return clientID;
    }


    public String getClientInfo() {
        return clientID + (isCoordinator ? " (Coordinator)" : "");
    }


    protected void sendMessage(String type, String message) {
        if (out != null) {
            out.println(type + " " + message);
            out.flush();
        }
    }

    
    private void handleClientRequest(String message) {
        String timestamp = Utils.getCurrentTimestamp();  // Get the current timestamp
        if (message.startsWith("requestMemberList")) {
            sendMessage("memberList", ChatServer.getClientList());
        }
        else if (message.startsWith("@")) {
            handleSendingPrivateMessage(message);
        }
        else if (message.startsWith("requestCoordinatorMemberList")) {
            handleRequestingListFromCoordinator(message);
        }
        else if (message.startsWith("sendCoordinatorMemberList")) {
            handleSendingListToClient(message);
        }
        else if (message.startsWith("assignUserID")) {
            if (handleAssigningUsername(message)){
                ChatServer.clients.add(this);
            }
        }
        else {  // If message doesn't start with any reserved prefix
            if (clientID != null) {
                String formattedMessage = "[" + timestamp + "] " + clientID + ": " + message;
                ChatServer.broadcastMessage(formattedMessage);
            }
        }
    }

    
    private void handleSendingPrivateMessage(String message) {
        String timestamp = Utils.getCurrentTimestamp();
        // message = "@username <message>"
        String[] parts = message.split(" ", 2);
        if (!(parts.length == 2)) {
            sendMessage("text", "Invalid format! Use @username message");
        }

        // substring(1) ignores the first character because parts[0] starts with "@"
        String recipient = parts[0].substring(1);  // recipient username
        String payload = parts[1];  // The message to be sent

        // Display the sender's message on their chat window for feedback purposes.
        sendMessage("text", "[" + timestamp + "]" + " [PM to " + recipient + "]: " + payload);
        // Display the sender's message on the recipient's chat window.
        for (ClientHandler client : ChatServer.clients) {
            if (client.clientID.equalsIgnoreCase(recipient)) {
                client.sendMessage("text", "[" + timestamp + "]" + " [Private] "
                                    + this.clientID + ": " + payload);
                return;
            }
        }
        // Exception Handling if username does not exist
        sendMessage("text", "User '" + recipient + "' not found!");
    }

    
    private void handleRequestingListFromCoordinator(String message) {
        String[] parts = message.split(" ");
        String instruction = parts[0];
        String userID = parts[1];
        ChatServer.coordinator.sendMessage(instruction, userID); // Send the same message to the coordinator
    }


    private void handleSendingListToClient(String message) {
        String[] parts = message.split(" ", 3);
        String recipient = parts[1];
        String strMemberList = parts[2];
        for (ClientHandler client : ChatServer.clients) {
            if (client.clientID.equalsIgnoreCase(recipient)) {
                client.sendMessage(
                        "sendCoordinatorMemberList",
                        strMemberList.replaceAll("[\\[\\]]", "")  // Remove brackets [] from the string
                );
            }
        }
    }


    private boolean handleAssigningUsername(String message) {
        String[] parts = message.split(" ");
        String username = parts[1];
        for (ClientHandler client : ChatServer.clients) {
            if (client.clientID != null && client.clientID.equalsIgnoreCase(username)) {
                sendMessage("exit", "The username is taken");
                return false;
            }
        }

        // Assign username if not taken
        this.clientID = username;
        sendMessage(
                "text",
                "Welcome " +
                        this.clientID +
                        "! Type '/list' to see a list of clients and '@username <message>' for private messages."
        );
        return true;
    }


    @Override
    public void run() {
        try {
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            // Assign coordinator if it's the first client
            if (ChatServer.coordinator == null) {
                ChatServer.coordinator = this;
                this.setCoordinator(true);
            }

            // Start listening for client requests
            String message;
            while ((message = in.readLine()) != null) {
                handleClientRequest(message);
            }
        } catch (IOException e) {
            System.out.println(clientID + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            ChatServer.removeClient(this);
        }
    }
}
