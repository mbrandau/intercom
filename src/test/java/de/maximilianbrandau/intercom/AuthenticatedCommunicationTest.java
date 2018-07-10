package de.maximilianbrandau.intercom;

import de.maximilianbrandau.intercom.authentication.AuthenticationResult;
import de.maximilianbrandau.intercom.authentication.Authenticator;
import de.maximilianbrandau.intercom.client.IntercomClient;
import de.maximilianbrandau.intercom.codec.IntercomCodec;
import de.maximilianbrandau.intercom.codec.defaults.IntercomStringCodec;
import de.maximilianbrandau.intercom.server.Client;
import de.maximilianbrandau.intercom.server.DefaultClientInitializer;
import de.maximilianbrandau.intercom.server.IntercomServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class AuthenticatedCommunicationTest {

    private static final String HOST = "localhost";

    private IntercomCodec<String> codec;
    private IntercomServer<String, String, Client> server;
    private IntercomClient<String, String> clientWithCorrectPassword;
    private CompletableFuture<Event<String>> completableFuture = new CompletableFuture<>();

    @Before
    public void setUp() {
        codec = new IntercomStringCodec();

        final String password = "helloworld";

        server = Intercom
                .server(codec, codec)
                .authenticationHandler(authenticationData ->
                        authenticationData.equals(password) ? AuthenticationResult.success() : AuthenticationResult.failure("Wrong password")
                )
                .build(new DefaultClientInitializer<>());
        System.out.printf("Listening on port %d\n", server.getPort());
        clientWithCorrectPassword = Intercom
                .client(HOST, server.getPort(), codec, codec)
                .authenticator(new Authenticator<>() {
                    @Override
                    public String authenticate() {
                        return password;
                    }

                    @Override
                    public void handleAuthenticationResult(AuthenticationResult result) {
                        assertTrue(result.isSuccess());
                    }
                })
                .eventHandler(event -> completableFuture.complete(event))
                .build();
    }

    @Test
    public void shouldFailOnWrongPassword() {
        IntercomClient<String, String> client = Intercom
                .client(HOST, server.getPort(), codec, codec)
                .authenticator(new Authenticator<>() {
                    @Override
                    public String authenticate() {
                        return "wrongpassword";
                    }

                    @Override
                    public void handleAuthenticationResult(AuthenticationResult result) {
                        assertFalse(result.isSuccess());
                        assertEquals("Wrong password", result.getError());
                    }
                })
                .build();

        client.close();
    }

    @Test
    public void requestHandler() throws ExecutionException, InterruptedException {
        server.getRequestHandlerRegistry().registerHandler("toUppercase", (request, response) -> {
            response.setData(request.getData().toUpperCase());
            response.end();
        });

        assertEquals("TEST", clientWithCorrectPassword.request("toUppercase").data("test").send().get().getData());
    }

    @Test
    public void pushEventHandler() throws InterruptedException, ExecutionException, TimeoutException {
        server.pushEvent(new Event<>("test", "Hello World!"));
        Event<String> event = completableFuture.get(1, TimeUnit.SECONDS);
        assertEquals("test", event.getEvent());
        assertEquals("Hello World!", event.getData());
    }

    @After
    public void tearDown() {
        clientWithCorrectPassword.close();
        server.close();
    }

}
