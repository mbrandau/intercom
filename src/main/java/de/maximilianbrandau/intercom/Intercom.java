package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.server.IntercomServer;

public class Intercom {

    /**
     * Convenience method to create an {@link IntercomClient.Builder}.
     *
     * @param host The hostname of your intercom server
     * @param port The port the intercom server is listening on
     * @return An intercom client builder
     */
    public static IntercomClient.Builder client(String host, int port) {
        return new IntercomClient.Builder(host, port);
    }

    /**
     * Convenience method to create an {@link IntercomServer.Builder}.
     * @param port The port that the server should listen on
     * @return An intercom server builder
     */
    public static IntercomServer.Builder server(int port) {
        return new IntercomServer.Builder(port);
    }

}
