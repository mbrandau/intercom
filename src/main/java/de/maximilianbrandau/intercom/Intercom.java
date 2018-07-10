package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.server.IntercomServer;

import javax.annotation.Nullable;

public class Intercom {

    /**
     * Convenience method to create an {@link IntercomClient.Builder}.
     *
     * @param host                The hostname of your intercom server
     * @param port                The port the intercom server is listening on
     * @param codec               The codec to encode and decode request and pushEvent event data
     * @param authenticationCodec The codec to encode and decode authentication data
     * @param <T>                 The type of data that is used in requests and events
     * @param <A>                 The type of data that is used to authenticate the client
     * @return An intercom client builder
     */
    public static <T, A> IntercomClient.Builder<T, A> client(String host, int port, IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
        return new IntercomClient.Builder<>(host, port, codec, authenticationCodec);
    }

    /**
     * Convenience method to create an {@link IntercomServer.Builder}.
     *
     * @param port                The port that the server should listen on
     * @param codec               The codec to encode and decode request and pushEvent event data
     * @param authenticationCodec The codec to encode and decode authentication data
     * @param <T>                 The type of data that is used in requests and events
     * @param <A>                 The type of data that is send by the client for authentication
     * @return An intercom server builder
     */
    public static <T, A> IntercomServer.Builder<T, A> server(int port, IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
        return new IntercomServer.Builder<>(port, codec, authenticationCodec);
    }

}
