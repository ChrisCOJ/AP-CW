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
        }
    }

    private void handleClientRequest(String message) {
        if (message.equalsIgnoreCase("requestMemberList")) {
            sendMessage("memberList:", ChatServer.getClientList());
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

            // Ask for ID
            out.println("text: Enter your ID:");
            clientId = in.readLine();
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new IOException("Client did not provide an ID");
            }
            out.println(
                "text: " +
                "Welcome " +
                clientId +
                "! Type 'list' to see users, '@user message' for private messages, or 'exit' to leave."
            );

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
