import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

class ServerPing implements Runnable {

    private final PrintWriter out;

    public ServerPing(PrintWriter out) {
        this.out = out;
    }

    @Override
    public void run() {
        while (!Client.stopThreads) {
            if (Client.isCoordinator) {
                out.println("requestMemberList");
                out.flush();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}


class Listener implements Runnable {
    private final BufferedReader in;
    private final PrintWriter out;

    public Listener(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    private void handleServerRequest(String serverMessage) throws RuntimeException {
        String[] msgParts = serverMessage.split(" ", 2);
        switch (msgParts[0]) {
            case ("text"):
                System.out.println("\n" + msgParts[1]); // Print new message
                System.out.print("> "); // Show prompt again
                break;
            case ("memberList"):
                Client.memberList = msgParts[1].split("§");
                break;
            case ("activateCoordinator"):
                Client.isCoordinator = true;
                break;
            case ("requestCoordinatorMemberList"):
                if (Client.isCoordinator) {
                    // msgParts[1] = userID associated with the request
                    out.println(
                            "sendCoordinatorMemberList " + msgParts[1] +
                            " " + Arrays.toString(Client.memberList)
                    );
                    out.flush();
                }
                break;
            case ("sendCoordinatorMemberList"):
                System.out.println("*** Members List ***");
                for (String line : msgParts[1].split(", ")) {
                    System.out.println(line);
                }
                break;
            case ("exit"):
                Client.stopThreads = true;
                throw new RuntimeException(msgParts[1]);
        }
    }

    public void run() {
        try {
            String serverMessage;
            while (!Client.stopThreads && (serverMessage = in.readLine()) != null) {
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


public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<Thread> threads = new ArrayList<>();
    protected static String[] memberList;
    protected static boolean isCoordinator = false;
    protected static volatile boolean stopThreads = false;

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

            // Start threads
            Thread listener = new Thread(new Listener(in, out));
            listener.start();
            threads.add(listener);

            Thread serverPing = new Thread(new ServerPing(out));
            serverPing.start();
            threads.add(serverPing);


            System.out.println("Connected to server.");
            out.println("assignUserID " + userId);
            out.flush();

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
            while (!stopThreads) {
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
            for (Thread thread : threads) {
                thread.join();
            }

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Client("127.0.0.1", 50000); // Change IP and port if needed
    }
}
