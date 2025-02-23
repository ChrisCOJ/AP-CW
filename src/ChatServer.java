import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatServer {
    private static final int PORT = 50000; // Port number
    private static final Set<ClientHandler> clients = new CopyOnWriteArraySet<>();
    private static ClientHandler coordinator = null;

    public static void main(String[] args) {
        System.out.println("Server started on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Waiting for a client to connect...");

                Socket socket = serverSocket.accept();
                System.out.println("New client connected!");

                ClientHandler client = new ClientHandler(socket);
                clients.add(client);

                // Assign coordinator if it's the first client
                if (coordinator == null) {
                    coordinator = client;
                    client.setCoordinator(true);
                }

                new Thread(client).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    static void sendPrivateMessage(String recipientId, String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client.getClientId().equalsIgnoreCase(recipientId)) {
                client.sendMessage("[Private] " + sender.getClientId() + ": " + message);
                return;
            }
        }
        sender.sendMessage("User '" + recipientId + "' not found!");
    }

    static void removeClient(ClientHandler client) {
        clients.remove(client);

        // If coordinator leaves, assign a new one
        if (client == coordinator) {
            coordinator = clients.isEmpty() ? null : clients.iterator().next();
            if (coordinator != null) {
                coordinator.setCoordinator(true);
            }
        }

        broadcastMessage(client.getClientId() + " has left the chat.", null);
    }

    static String getClientList() {
        StringBuilder list = new StringBuilder("Connected clients:\n");
        for (ClientHandler client : clients) {
            list.append(client.getClientInfo()).append("\n");
        }
        return list.toString();
    }
}
