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


public class ClientHandlerTests {

    private ClientHandler clientConn;  // Mock clientHandler instance used to test its methods.
    private ByteArrayOutputStream clientOutputBytes;
    private PrintWriter mockOut;


    @BeforeEach
    void setUp() {
        clientOutputBytes = new ByteArrayOutputStream();
        mockOut = new PrintWriter(new OutputStreamWriter(clientOutputBytes), true);

        clientConn = new ClientHandler(new Socket());
        clientConn.setClientID("TestClient");

        // Populate the server client list with a few mock ClientHandlers.
        ChatServer.addClient(clientConn);
        for (int i = 0; i < 3; ++i) {
            ClientHandler clientConn = new ClientHandler(new Socket());
            clientConn.setClientID(Integer.toString(i));  // Give mock clientHandlers some test names
            ChatServer.addClient(clientConn);
            clientConn.setOutputStream(mockOut);
        }
        clientConn.setOutputStream(mockOut);
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


    @Test
    void testHandleClientRequest_sendMemberList() {
        // Assume the client is the coordinator and can send such a request
        String mockClientRequest = "requestMemberList";

        clientConn.handleClientRequest(mockClientRequest);

        String output = clientOutputBytes.toString().trim();
        assertEquals("memberList " + ChatServer.getClientList(), output, "Message should be" +
                "structured as such: <memberList List-As-String>");
    }


    @Test
    void testHandleClientRequest_sendValidPrivateMessage() {
        // Simulates sending a private message "Hello" to a client with username "1"
        String mockClientRequest = "@1 Hello";
        String message = mockClientRequest.split(" ", 2)[1];

        clientConn.handleClientRequest(mockClientRequest);

        String output = clientOutputBytes.toString().trim();
        System.out.println(output);
        assertTrue(output.contains("[Private] " + clientConn.getClientID() + ": " + message),
                "Console output associated with private messages");
    }


    @Test
    void testHandleClientRequest_sendInvalidPrivateMessage() {
        // Simulates sending an invalid private message "Hello" to a non-existent client username
        String mockClientRequest = "@not-exist Hello";
        String username = mockClientRequest.split(" ", 2)[0].substring(1);

        clientConn.handleClientRequest(mockClientRequest);

        String output = clientOutputBytes.toString().trim();
        assertEquals("text User '" + username + "' not found!", output,
                "Console output associated with private messages");
    }
}
