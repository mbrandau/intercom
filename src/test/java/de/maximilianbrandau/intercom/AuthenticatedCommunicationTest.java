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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AuthenticatedCommunicationTest {

    private static final int PORT = 8080;
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
                .server(PORT, codec, codec)
                .authenticationHandler(authenticationData ->
                        authenticationData.equals(password) ? AuthenticationResult.success() : AuthenticationResult.failure("Wrong password")
                )
                .build(new DefaultClientInitializer<>());

        clientWithCorrectPassword = Intercom
                .client(HOST, PORT, codec, codec)
                .authenticator(new Authenticator<>() {
                    @Override
                    public String authenticate() {
                        return password;
                    }

                    @Override
                    public void handleAuthenticationResult(AuthenticationResult result) {

                    }
                })
                .eventHandler(event -> completableFuture.complete(event))
                .build();
    }

    @Test
    public void shouldFailOnWrongPassword() {
        IntercomClient<String, String> client = Intercom
                .client(HOST, PORT, codec, codec)
                .authenticator(new Authenticator<>() {
                    @Override
                    public String authenticate() {
                        return "wrongpassword";
                    }

                    @Override
                    public void handleAuthenticationResult(AuthenticationResult result) {
                        if (result.isSuccess()) Assert.fail("Authentication should fail");
                        else System.out.println(result.getError());
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

        Assert.assertEquals("TEST", clientWithCorrectPassword.request("toUppercase").data("test").send().get().getData());
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
        clientWithCorrectPassword.close();
        server.close();
    }

}
