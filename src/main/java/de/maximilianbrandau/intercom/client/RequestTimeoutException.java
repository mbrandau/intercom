package de.maximilianbrandau.intercom.client;

public class RequestTimeoutException extends RuntimeException {

    public RequestTimeoutException() {
        super("Request timed out");
    }

    public RequestTimeoutException(long time) {
        super("Request timed out after " + ((double) time / 1000.0) + " seconds");
    }
}
