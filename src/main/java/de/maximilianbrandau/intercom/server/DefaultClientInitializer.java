package de.maximilianbrandau.intercom.server;

import io.netty.channel.Channel;

public class DefaultClientInitializer<T, A> implements ClientInitializer<T, A, Client> {
    @Override
    public Client<T, A> init(IntercomServer<T, A, Client> server, Channel channel) {
        return new Client<>(server, channel);
    }
}
