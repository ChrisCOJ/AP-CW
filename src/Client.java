import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

class ServerPing implements Runnable {

    private volatile boolean stopThread = false;
    final PrintWriter out;

    public ServerPing(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void run() {
        while (!stopThread) {
            out.println("requestMemberList");
            out.flush();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String[] memberList;

    private void handleServerRequest(String serverMessage) {
        String[] msgParts = serverMessage.split(" ", 2);
        if (serverMessage.startsWith("text:")) {
            System.out.println("\n" + msgParts[1]); // Print new message
            System.out.print("> "); // Show prompt again
        } else if (serverMessage.startsWith("memberList:")) {
            memberList = parseStrToList(msgParts[1]);
        }
    }

    private String[] parseStrToList(String serverMessage) {
        String[] list = serverMessage.split("§");

        return list;
    }

    public Client(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server.");
            System.out.print("Enter your ID: ");
            Scanner scanner = new Scanner(System.in);
            String userId = scanner.nextLine();
            out.println(userId);

            ServerPing serverPing = new ServerPing(out);
            new Thread(serverPing).start();

            // Start a new thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        handleServerRequest(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            Runtime.getRuntime()
                .addShutdownHook(
                    new Thread(() -> {
                        try {
                            // Cleanup
                            scanner.close();
                            socket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                );

            // Main thread handles user input
            while (true) {
                String message = scanner.nextLine();
                out.println(message);
                if (message.equalsIgnoreCase("list")) {
                    for (String id : memberList) {
                        System.out.println(id);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 50000); // Change IP and port if needed
    }
}
