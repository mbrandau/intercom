package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.codec.AuthenticationEncodingMechanism;

public interface AuthenticationHandler<T> extends AuthenticationEncodingMechanism<T> {

    boolean authenticate(T authenticationData);

}
