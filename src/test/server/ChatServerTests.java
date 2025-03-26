package test.server;

import main.server.ChatServer;
import main.server.ClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;


public class ChatServerTests {

    ClientHandler coordinatorClientHandler;  // Previous coordinator
    ClientHandler nextCoordinatorClientHandler;  // Next coordinator to be assigned
    ByteArrayOutputStream clientOutputBytes;
    PrintWriter mockOut;


    @BeforeEach
    void setUp() {
        clientOutputBytes = new ByteArrayOutputStream();
        mockOut = new PrintWriter(new OutputStreamWriter(clientOutputBytes), true);

        nextCoordinatorClientHandler = new ClientHandler(new Socket());  // Coordinator to be reassigned
        nextCoordinatorClientHandler.setClientID("NextCoordinator");
        ChatServer.addClient(nextCoordinatorClientHandler);

        coordinatorClientHandler = new ClientHandler(new Socket());
        coordinatorClientHandler.setClientID("PreviousCoordinator");
        coordinatorClientHandler.setOutputStream(mockOut);  // Capture the output into clientOutputBytes
        coordinatorClientHandler.setCoordinator(true);
        ChatServer.setCoordinator(coordinatorClientHandler);
    }


    @AfterEach
    void clean() throws IOException {
        mockOut.close();
        clientOutputBytes.close();
    }


    @Test
    void testGetClientList() {
        String output = ChatServer.getClientList();

        assertEquals(nextCoordinatorClientHandler.getClientID() + ";;", output,
                     "getClientList should return the client usernames from the server client list" +
                             "separated by ;;");
    }


    @Test
    void testReassigningCoordinator() {
        ChatServer.reassignCoordinator(coordinatorClientHandler);

        String actualOutput = ChatServer.getClientList();
        assertEquals(nextCoordinatorClientHandler.getClientID() + " (Coordinator);;", actualOutput,
                        "A random client in the client list should be assigned to be the new coordinator. " +
                        "Since there is only one client inside the mock client list, the known client" +
                        "must be assigned as a coordinator. getClientList() must therefore return" +
                        "newCoordinatorUsername (Coordinator);;");

        String output = clientOutputBytes.toString().trim();
        assertEquals("text *** You are now the coordinator ***\n" + "activateCoordinator", output,
                "Output should include feedback to the coordinator, and a command to activate " +
                        "the coordinator client-side");
    }
}
