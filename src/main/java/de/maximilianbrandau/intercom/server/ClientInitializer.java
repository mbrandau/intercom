package de.maximilianbrandau.intercom.server;

import io.netty.channel.Channel;

public interface ClientInitializer<T, A, C extends Client> {

    C init(IntercomServer<T, A, C> server, Channel channel);

}
