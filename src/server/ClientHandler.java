package server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private boolean isCoordinator = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void setCoordinator(boolean status) {
        isCoordinator = status;
        sendMessage("text", "*** You are now the coordinator ***");
        sendMessage("activateCoordinator", "");
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientInfo() {
        return clientId + (isCoordinator ? " (Coordinator)" : "");
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
            handleRequestListFromCoordinator(message);
        }
        else if (message.startsWith("sendCoordinatorMemberList")) {
            handleSendingListToClient(message);
        }
        else if (message.startsWith("assignUserID")) {
            if (handleAssigningUserID(message)){
                ChatServer.clients.add(this);
            }
        }
        else {  // If message doesn't start with any reserved prefix
            if (clientId != null) {
                String formattedMessage = "[" + timestamp + "] " + clientId + ": " + message;
                ChatServer.broadcastMessage(formattedMessage);
            }
        }
    }
    
    private void handleSendingPrivateMessage(String message) {
        // message = "@username <message>"
        String[] parts = message.split(" ", 2);
        if (parts.length > 1) {
            String username = parts[0];
            String payload = parts[1];
            ChatServer.sendPrivateMessage(
                    username.substring(1),  // PM starts with "@", so ignore the first character
                    payload,
                    this
            );
        } else {
            sendMessage("text", "Invalid format! Use @username message");
        }
    }
    
    private void handleRequestListFromCoordinator(String message) {
        String[] parts = message.split(" ");
        String instruction = parts[0];
        String userID = parts[1];
        ChatServer.coordinator.sendMessage(instruction, userID); // Send the same message to the coordinator
    }

    private void handleSendingListToClient(String message) {
        String[] parts = message.split(" ", 3);
        String userID = parts[1];
        String strMemberList = parts[2];
        for (ClientHandler client : ChatServer.clients) {
            if (client.clientId.equals(userID)) {
                client.sendMessage(
                        "sendCoordinatorMemberList",
                        strMemberList.replaceAll("[\\[\\]]", "")  // Remove brackets [] from the string
                );
            }
        }
    }

    private boolean handleAssigningUserID(String message) {
        String[] parts = message.split(" ");
        String userID = parts[1];
        for (ClientHandler client : ChatServer.clients) {
            if (client.clientId != null && client.clientId.equals(userID)) {
                sendMessage("exit", "The username is taken");
                return false;
            }
        }
        clientId = parts[1];
        // Ask for ID
        sendMessage(
                "text",
                "Welcome " +
                        clientId +
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
            System.out.println(clientId + " disconnected.");
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
            ChatServer.removeClient(this);
        }
    }
}
