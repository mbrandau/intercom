package de.maximilianbrandau.intercom.client;

import de.maximilianbrandau.intercom.Event;

public interface EventHandler<T> {

    void handleEvent(Event<T> event);

}
