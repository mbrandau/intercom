package de.maximilianbrandau.intercom;

public interface IntercomRequestHandler<T> {

    void handleRequest(Request<T> request, OutgoingResponse<T> response);

}
