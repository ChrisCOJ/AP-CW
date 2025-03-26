package main.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {

    protected volatile boolean stopThreads = false;
    public volatile boolean receivedList = false;
    protected volatile boolean isCoordinator = false;
    protected final String username;
    protected String[] memberList;  // This list is empty for non-coordinator clients

    public Client(String username) {
        this.username = username;
    }

    /////////////// Getters / Setters ///////////////
    public String getUsername() {
        return username;
    }
    ///
    public String[] getMemberList() {
        return memberList;
    }
    ///
    public void setMemberList(String[] list) {
        memberList = list;
    }
    ///
    public boolean isCoordinator() {
        return isCoordinator;
    }
    ///
    public void setCoordinator(boolean status) {
        isCoordinator = status;
    }
    /////////////////////////////////////////////////


    public void start(String serverAddress, int serverPort) {
        try {
            // Open channels of communication with the server
            Socket socket = new Socket(serverAddress, serverPort);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);

            ////////////// Start threads /////////////////
            ArrayList<Thread> threads = new ArrayList<>();
            ///
            Thread listener = new Thread(new ServerMessageListener(this, in, out));
            listener.start();
            threads.add(listener);
            ///
            Thread serverPing = new Thread(new ServerPing(this, out));
            serverPing.start();
            threads.add(serverPing);
            //////////////////////////////////////////////

            // Inform the server of the client's username
            out.println("assignUserID " + username);

            //////  Main thread handles user input ///////
            while (!stopThreads) {
                String message = scanner.nextLine();
                handleUserInput(out, message);
            }
            //////////////////////////////////////////////

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


    public void handleUserInput(PrintWriter out, String message) throws InterruptedException {
        System.out.print("\033[A\033[2K");  // Move cursor up and clear the prompt line (i.e. 'Chris: ')

        if (!message.equalsIgnoreCase("/list")) {
            out.println(message);  // Send message to server if it doesn't equal "list" command
        }
        // If coordinator types '/list', print the list stored locally
        else if (isCoordinator) {
            System.out.println("\n*** Member List ***");
            for (String id : memberList) {
                System.out.println(id);
            }
            System.out.print("\n");
            System.out.print(username + ": ");  // Display a message prompt
            // If non-coordinator types '/list', request the list from the coordinator client
        } else {
            out.println("requestCoordinatorMemberList " + username);
            // Wait to receive the member list from the server
            while (!receivedList) {
                Thread.sleep(100);
            }
            receivedList = false;
        }
    }


    public static void main(String[] args) {
        // Let user choose their username
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty. Disconnecting...");
            System.exit(1);
        }

        Client client = new Client(username);
        client.start("127.0.0.1", 50000);
    }

}
