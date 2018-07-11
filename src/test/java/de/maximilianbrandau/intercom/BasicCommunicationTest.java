/*
 * Copyright (c) 2017-2018 Maximilian Brandau
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
    private CompletableFuture<Event<String>> completableFuture;

    @Before
    public void setUp() {
        IntercomCodec<String> codec = new IntercomStringCodec();
        completableFuture = new CompletableFuture<>();
        server = Intercom.server(codec, null).build(new DefaultClientInitializer<>());
        System.out.printf("Listening on port %d\n", server.getPort());
        client = Intercom
                .client("localhost", server.getPort(), codec, null)
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
