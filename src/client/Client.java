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
    protected volatile boolean receivedList = false;
    protected final String userID;

    public Client(String serverAddress, int serverPort) {
        System.out.print("Enter your username: ");
        Scanner scanner = new Scanner(System.in);
        userID = scanner.nextLine();

        try {
            // Open channels of communication with the server
            socket = new Socket(serverAddress, serverPort);
            in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            out = new PrintWriter(socket.getOutputStream(), true);

            // ********* Start threads *********
            Thread listener = new Thread(new Listener(this, in, out));
            listener.start();
            threads.add(listener);

            Thread serverPing = new Thread(new ServerPing(this, out));
            serverPing.start();
            threads.add(serverPing);
            // *********************************

            System.out.println("Connected to server.");
            out.println("assignUserID " + userID);
            out.flush();


            // Main thread handles user input
            while (!stopThreads) {
                String message = scanner.nextLine();

                if (!message.equalsIgnoreCase("/list")) {
                    out.println(message);  // Broadcast message if it doesn't equal "list" command
                }
                // If coordinator types '/list', print the list stored locally
                else if (isCoordinator) {
                    System.out.println("\n*** Member List ***");
                    for (String id : memberList) {
                        System.out.println(id);
                    }
                    System.out.print("\n");
                // If non-coordinator types '/list', request the list from the coordinator client
                } else {
                    out.println("requestCoordinatorMemberList " + userID);
                    out.flush();
                    while (!receivedList) {
                        Thread.sleep(10);
                    }
                    receivedList = false;
                }

                System.out.print(userID + ": ");  // Display a message prompt
            }

            // Cleanup
            // Wait for threads to finish execution before exiting gracefully
            for (Thread thread : threads) {
                thread.join();
            }
            scanner.close();
            socket.close();

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
