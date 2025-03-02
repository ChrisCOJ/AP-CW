import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientId;
    private boolean isCoordinator = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void setCoordinator(boolean status) {
        isCoordinator = status;
        sendMessage("text", "You are now the coordinator.");
        sendMessage("activateCoordinator", "");
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientInfo() {
        return clientId + (isCoordinator ? " (Coordinator)" : "");
    }

    public void sendMessage(String type, String message) {
        if (out != null) {
            out.println(type + " " + message);
            out.flush();
        }
    }

    private void handleClientRequest(String message) {
        if (message.startsWith("requestMemberList")) {
            sendMessage("memberList", ChatServer.getClientList());
        } else if (message.startsWith("@")) {
            String[] parts = message.split(" ", 2);
            if (parts.length > 1) {
                ChatServer.sendPrivateMessage(
                    parts[0].substring(1),
                    parts[1],
                    this
                );
            } else {
                sendMessage("text", "Invalid format! Use @username message");
            }
        } else if (message.startsWith("requestCoordinatorMemberList")) {
            String[] parts = message.split(" ");
            String instruction = parts[0];
            String userID = parts[1];
            ChatServer.coordinator.sendMessage(instruction, userID); // Send the same message to the coordinator
        } else if (message.startsWith("sendCoordinatorMemberList")) {
            String[] parts = message.split(" ", 3);
            for (ClientHandler client : ChatServer.clients) {
                if (client.clientId.equals(parts[1])) {
                    client.sendMessage(
                        "sendCoordinatorMemberList",
                        parts[2].replaceAll("[\\[\\]]", "")
                    );
                }
            }
        } else if (message.startsWith("assignUserID")) {
            String[] parts = message.split(" ");
            clientId = parts[1];
            // Ask for ID
            sendMessage(
                "text",
                "Welcome " +
                clientId +
                "! Type 'list' to see users, '@user message' for private messages, or 'exit' to leave."
            );
        } else {
            ChatServer.broadcastMessage(clientId + ": " + message, this);
        }
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
