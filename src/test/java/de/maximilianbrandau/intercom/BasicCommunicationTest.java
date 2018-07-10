package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.defaults.IntercomStringCodec;
import de.maximilianbrandau.intercom.server.Client;
import de.maximilianbrandau.intercom.server.DefaultClientInitializer;
import de.maximilianbrandau.intercom.server.IntercomServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BasicCommunicationTest {

    private IntercomServer<String, ?, Client> server;
    private IntercomClient<String, ?> client;
    private CompletableFuture<Event<String>> completableFuture = new CompletableFuture<>();

    @Before
    public void setUp() {
        IntercomCodec<String> codec = new IntercomStringCodec();

        server = Intercom.server(8080, codec, null).build(new DefaultClientInitializer<>());
        client = Intercom
                .client("localhost", 8080, codec, null)
                .eventHandler(event -> completableFuture.complete(event))
                .build();
    }

    @Test
    public void requestHandler() throws ExecutionException, InterruptedException {
        server.getRequestHandlerRegistry().registerHandler("toUppercase", (request, response) -> {
            response.setData(request.getData().toUpperCase());
            response.end();
        });

        Assert.assertEquals("TEST", client.request("toUppercase").data("test").send().get().getData());
    }

    @Test
    public void pushEventHandler() throws InterruptedException, ExecutionException, TimeoutException {
        server.pushEvent(new Event<>("test", "Hello World!"));
        Event<String> event = completableFuture.get(1, TimeUnit.SECONDS);
        Assert.assertEquals("test", event.getEvent());
        Assert.assertEquals("Hello World!", event.getData());
    }

    @After
    public void tearDown() {
        client.close();
        server.close();
    }

}
