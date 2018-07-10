package de.maximilianbrandau.intercom.authentication;

public interface Authenticator<A> {

    A authenticate();

    void handleAuthenticationResult(AuthenticationResult result);

}
