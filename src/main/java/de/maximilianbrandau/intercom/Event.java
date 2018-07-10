package de.maximilianbrandau.intercom;

/**
 * An Event is pushed from the {@link de.maximilianbrandau.intercom.server.IntercomServer} to all connected {@link de.maximilianbrandau.intercom.client.IntercomClient}s
 *
 * @param <T> The type of the event data
 */
public class Event<T> {

    private final String event;
    private final T data;

    public Event(String event, T data) {
        this.event = event;
        this.data = data;
    }

    public String getEvent() {
        return event;
    }

    public T getData() {
        return data;
    }

}
