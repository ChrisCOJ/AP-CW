import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

class ServerPing implements Runnable {

    private volatile boolean stopThread = false;
    private final PrintWriter out;

    public ServerPing(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void run() {
        while (!stopThread) {
            if (Client.isCoordinator) {
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
}

public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String[] memberList;
    protected static boolean isCoordinator = false;

    private void handleServerRequest(String serverMessage) {
        String[] msgParts = serverMessage.split(" ", 2);
        switch (msgParts[0]) {
            case ("text"):
                System.out.println("\n" + msgParts[1]); // Print new message
                System.out.print("> "); // Show prompt again
                break;
            case ("memberList"):
                memberList = parseStrToList(msgParts[1]);
                break;
            case ("activateCoordinator"):
                isCoordinator = true;
                break;
            case ("requestCoordinatorMemberList"):
                if (isCoordinator) {
                    // msgParts[1] = userID associated with the request
                    out.println(
                        "sendCoordinatorMemberList " +
                        msgParts[1] +
                        " " +
                        Arrays.toString(memberList)
                    );
                    out.flush();
                }
                break;
            case ("sendCoordinatorMemberList"):
                System.out.println("*** Members List ***");
                for (String line : msgParts[1].split(", ")) {
                    System.out.println(line);
                }
        }
    }

    private String[] parseStrToList(String serverMessage) {
        String[] list = serverMessage.split("§");

        return list;
    }

    public Client(String serverAddress, int serverPort) {
        System.out.print("Enter your ID: ");
        Scanner scanner = new Scanner(System.in);
        String userId = scanner.nextLine();

        try {
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server.");
            out.println("assignUserID " + userId);
            out.flush();

            // Start listening for server requests
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

            ServerPing serverPing = new ServerPing(out);
            new Thread(serverPing).start();

            // Handles Client exit
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
                    if (isCoordinator) {
                        System.out.println("*** Member List ***");
                        for (String id : memberList) {
                            System.out.println(id);
                        }
                    } else {
                        out.println("requestCoordinatorMemberList " + userId);
                        out.flush();
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
