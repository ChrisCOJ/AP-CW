package client;

import java.io.PrintWriter;


class ServerPing implements Runnable {
    /* Thread that sends a message to the server requesting the most recent member list each X unit of time.
    *  This request will only be sent by the coordinator client */

    private final PrintWriter out;
    private final Client client;

    public ServerPing(Client client, PrintWriter out) {
        this.out = out;
        this.client = client;
    }

    @Override
    public void run() {
        while (!client.stopThreads) {
            if (client.isCoordinator) {
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