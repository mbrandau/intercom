package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.encoding.AuthenticationEncodingMechanism;

public interface AuthenticationHandler<T> extends AuthenticationEncodingMechanism<T> {

    boolean authenticate(T authenticationData);

}
