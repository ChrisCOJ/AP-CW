package test.client;

import org.junit.jupiter.api.*;
import main.client.Client;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;


public class ClientTests {

    private Client client;
    private ByteArrayOutputStream clientOutputBytes;
    private PrintWriter mockClientOutput;


    @BeforeEach
    void setUp() {
        client = new Client("TestUser");
        clientOutputBytes = new ByteArrayOutputStream();
        mockClientOutput = new PrintWriter(new OutputStreamWriter(clientOutputBytes), true);
    }

    @AfterEach
    void clean() {
        try {
            mockClientOutput.close();
            clientOutputBytes.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testUserInput_sendRegularMessage() throws InterruptedException {
        String message = "Hello";
        client.handleUserInput(mockClientOutput, message);

        String clientOutput = clientOutputBytes.toString().trim();  // Capture the output
        assertEquals(message, clientOutput, "Client output should match the message sent.");
    }

    @Test
    void testUserInput_nonCoordinatorMemberList() throws InterruptedException {
        // Simulate non-coordinator user typing "/list"
        client.setCoordinator(false);
        client.receivedList = true;  // Prevent infinite loop inside handleUserInput
        String message = "/list";
        client.handleUserInput(mockClientOutput, message);

        String clientOutput = clientOutputBytes.toString().trim();  // Capture the output
        System.out.println(clientOutput);
        assertEquals("requestCoordinatorMemberList " + client.getUsername(), clientOutput,
                "Client output should match the message sent.");
    }

//    @Test
//    void testSendingUsername() {
//        String testUsername = "TestUsername";
//        scanner = new Scanner(new StringReader(testUsername));  // Simulate a user input: "TestUsername"
//        out =
//        assertEquals(testUsername, , "Check whether the correct username is being sent to the server");
//    }
}
