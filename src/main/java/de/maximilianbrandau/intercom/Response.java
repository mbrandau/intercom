package de.maximilianbrandau.intercom;

public class Response<T> {

    private final short status;
    private final T data;
    private final long duration;

    Response(short status, long duration, T data) {
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
