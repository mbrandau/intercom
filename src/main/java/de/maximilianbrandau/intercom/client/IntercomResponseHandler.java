package de.maximilianbrandau.intercom.client;

public interface IntercomResponseHandler<T> {

    void handleResponse(IntercomResponse<T> response);

    void handleError(Throwable throwable);

}
