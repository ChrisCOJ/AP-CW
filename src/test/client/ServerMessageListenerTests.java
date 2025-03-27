package test.client;

import main.client.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;


public class ServerMessageListenerTests {

    private ServerMessageListener serverMessageListener;
    private Client client;
    PrintWriter mockOut;
    ByteArrayOutputStream clientOutputBytes;
    ByteArrayInputStream clientInputBytes;
    BufferedReader mockIn;


    @BeforeEach
    void setUp() {
        client = new Client("TestClient");

        // Simulate input/output streams to be able to instantiate ServerMessageListener class.
        clientInputBytes = new ByteArrayInputStream(new byte[]{});
        mockIn = new BufferedReader(new InputStreamReader(clientInputBytes));

        clientOutputBytes = new ByteArrayOutputStream();
        mockOut = new PrintWriter(new OutputStreamWriter(clientOutputBytes), true);

        serverMessageListener = new ServerMessageListener(client, mockIn, mockOut);
    }

    @AfterEach
    void clean() {
        try {
            mockIn.close();
            clientInputBytes.close();
            mockOut.close();
            clientOutputBytes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testNormalMessage() {
        String mockMessage = "text Hello";  // "text" = instruction type | "Hello" = message

        ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
        PrintStream originalOut = System.out; // Store original System.out
        System.setOut(new PrintStream(sysOut));  // Now System.out.println messages will be redirected to sysOut

        serverMessageListener.handleServerRequest(mockMessage);
        String listenerOutput = sysOut.toString().trim();  // Capture the output from System.out inside Listener class
        assertTrue(listenerOutput.contains(mockMessage.split(" ", 2)[1] + "\n" + client.getUsername() + ":"),
                "Output should contain the message and <username: > prompt on a new line");

        System.setOut(originalOut);  // Restore the original System.out
    }

    @Test
    void testCoordinatorAssignment() {
        client.setCoordinator(false);  // Set coordinator to false as a baseline
        String mockMessage = "activateCoordinator";

        serverMessageListener.handleServerRequest(mockMessage);

        assertTrue(client.isCoordinator(), "Coordinator flag should be set to true");
    }
}
