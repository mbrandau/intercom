package de.maximilianbrandau.intercom.server;

public interface IntercomRequestHandler<T> {

    void handleRequest(IntercomRequest<T> request, IntercomResponse<T> response);

}
