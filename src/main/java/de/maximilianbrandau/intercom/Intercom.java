package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.server.IntercomServer;

public class Intercom {

    public static IntercomClient.Builder client(String host, int port) {
        return new IntercomClient.Builder(host, port);
    }

    public static IntercomServer.Builder server(int port) {
        return new IntercomServer.Builder(port);
    }

}
