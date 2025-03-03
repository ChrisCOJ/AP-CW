import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatServer {

    private static final int PORT = 50000; // Port number
    protected static final Set<ClientHandler> clients =
        new CopyOnWriteArraySet<>();
    protected static ClientHandler coordinator = null;

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Waiting for a client to connect...");

                Socket socket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler client = new ClientHandler(socket);
                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }


    protected static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage("text", message);
            }
        }
    }

    protected static void sendPrivateMessage(String recipientId, String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client.getClientId().equalsIgnoreCase(recipientId)) {
                client.sendMessage(
                        "text",
                        "[Private] " + sender.getClientId() + ": " + message
                );
                return;
            }
        }
        sender.sendMessage("text", "User '" + recipientId + "' not found!");
    }

    protected static void removeClient(ClientHandler client) {
        clients.remove(client);
        reassignCoordinator(client);

        if (client.getClientId() != null) {
            broadcastMessage(client.getClientId() + " has left the chat.", null);
        }
    }

    private static void reassignCoordinator(ClientHandler client) {
        // If coordinator leaves, assign a new one
        if (client == coordinator) {
            coordinator = clients.isEmpty() ? null : clients.iterator().next();
            if (coordinator != null) {
                coordinator.setCoordinator(true);
            }
        }
    }

    protected static String getClientList() {
        StringBuilder list = new StringBuilder();
        for (ClientHandler client : clients) {
            list.append(client.getClientInfo()).append("§");
        }
        return list.toString();

    }
}
