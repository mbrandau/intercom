package de.maximilianbrandau.intercom.authentication;

public class AuthenticationResult {

    private final String error;

    private AuthenticationResult(String error) {
        this.error = error;
    }

    public static AuthenticationResult success() {
        return new AuthenticationResult(null);
    }

    public static AuthenticationResult failure(String error) {
        return new AuthenticationResult(error);
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public String getError() {
        return error;
    }

}
