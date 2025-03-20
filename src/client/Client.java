package client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<Thread> threads = new ArrayList<>();
    protected String[] memberList;
    protected boolean isCoordinator = false;
    protected volatile boolean stopThreads = false;

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
            Thread listener = new Thread(new Listener(this, in, out));
            listener.start();
            threads.add(listener);

            Thread serverPing = new Thread(new ServerPing(this, out));
            serverPing.start();
            threads.add(serverPing);


            System.out.println("Connected to server.");
            out.println("assignUserID " + userId);
            out.flush();

            // Handles client.Client exit
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
