package de.maximilianbrandau.intercom;

public class AlreadyClosedException extends RuntimeException {

    public AlreadyClosedException(String message) {
        super(message + " already closed");
    }
}
