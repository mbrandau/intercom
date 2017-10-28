package de.maximilianbrandau.intercom.client;

public class IntercomResponse<T> {

    private final short status;
    private final T data;
    private long duration;

    IntercomResponse(short status, long duration, T data) {
        this.status = status;
        this.duration = duration;
        this.data = data;
    }

    public boolean isOk() {
        return status == 200;
    }

    public long getDuration() {
        return duration;
    }

    public T getData() {
        return data;
    }

}
