package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.codec.IntercomCodec;

public interface AuthenticationHandler<T> extends IntercomCodec<T> {

    boolean authenticate(T authenticationData);

}
