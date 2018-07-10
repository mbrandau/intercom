package de.maximilianbrandau.intercom.authentication;

public interface AuthenticationHandler<A> {

    AuthenticationResult authenticate(A authenticationData);

}
