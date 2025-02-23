import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server.");
            System.out.print("Enter your ID: ");
            Scanner scanner = new Scanner(System.in);
            String userId = scanner.nextLine();
            writer.println(userId);

            // Start a new thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = reader.readLine()) != null) {
                        System.out.println("\n" + serverMessage);  // Print new message
                        System.out.print("> "); // Show prompt again
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Main thread handles user input
            while (true) {
                String message = scanner.nextLine();
                writer.println(message);
                if (message.equalsIgnoreCase("exit")) {
                    break; // Exit if user types "exit"
                }
            }

            // Cleanup
            scanner.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 50000); // Change IP and port if needed
    }
}

