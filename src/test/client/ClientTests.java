package test.client;

import org.junit.jupiter.api.*;
import main.client.Client;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;


public class ClientTests {

    private Client client;
    private ByteArrayOutputStream clientOutputBytes;
    private PrintWriter mockOut;


    @BeforeEach
    void setUp() {
        client = new Client("TestUser");
        clientOutputBytes = new ByteArrayOutputStream();
        mockOut = new PrintWriter(new OutputStreamWriter(clientOutputBytes), true);
    }

    @AfterEach
    void clean() {
        try {
            mockOut.close();
            clientOutputBytes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Ensures the username is properly assigned. (works as expected)
    @Test
    void testClientConstructor() {
        assertEquals("TestUser", client.getUsername());
    }


    @Test
    void testUserInput_sendRegularMessage() throws InterruptedException {
        String message = "Hello";
        client.handleUserInput(mockOut, message);

        String clientOutput = clientOutputBytes.toString().trim();  // Capture the output
        assertEquals(message, clientOutput, "Client output should match the message sent.");
    }


    @Test
    void testUserInput_nonCoordinatorMemberList() throws InterruptedException {
        // Simulate non-coordinator user typing "/list"
        client.setCoordinator(false);
        client.receivedList = true;  // Prevent infinite loop inside handleUserInput

        String message = "/list";
        client.handleUserInput(mockOut, message);

        String clientOutput = clientOutputBytes.toString().trim();  // Capture the output
        assertEquals("requestCoordinatorMemberList " + client.getUsername(), clientOutput,
                "Client output should match the message sent.");
    }


    @Test
    void testHandleUserInputCoordinatorListRequest() throws InterruptedException {
        // Simulate coordinator user typing "/list"
        client.setCoordinator(true);
        client.receivedList = true;  // Prevent infinite loop inside handleUserInput
        client.setMemberList(new String[]{"User1", "User2"});
        String message = "/list";

        // We have to capture system output stream because when the list is requested by the coordinator
        // The list is printed using System.out.println()
        ByteArrayOutputStream sysOutBytes = new ByteArrayOutputStream();
        PrintStream originalOut = System.out; // Store original System.out
        PrintWriter sysOut = new PrintWriter(new OutputStreamWriter(sysOutBytes));
        System.setOut(new PrintStream(sysOutBytes));  // Now System.out.println messages will be redirected to sysOut

        client.handleUserInput(sysOut, message);

        String output = sysOutBytes.toString().trim();
        assertTrue(output.contains("\n*** Member List ***"));

        System.setOut(originalOut);  // Restore original sys output stream
    }
}
