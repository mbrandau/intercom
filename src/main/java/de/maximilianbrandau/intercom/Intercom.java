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
import de.maximilianbrandau.intercom.server.IntercomServer;

import javax.annotation.Nullable;

public class Intercom {

    /**
     * Convenience method to create an {@link IntercomClient.Builder}.
     *
     * @param host                The hostname of your intercom server
     * @param port                The port the intercom server is listening on
     * @param codec               The codec to encode and decode request and pushEvent event data
     * @param authenticationCodec The codec to encode and decode authentication data
     * @param <T>                 The data type that is used in requests and events
     * @param <A>                 The data type that is used to authenticate the client
     * @return A new intercom client builder
     */
    public static <T, A> IntercomClient.Builder<T, A> client(String host, int port, IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
        return new IntercomClient.Builder<>(host, port, codec, authenticationCodec);
    }

    /**
     * Convenience method to create an {@link IntercomServer.Builder}.
     *
     * @param codec               The codec to encode and decode request and pushEvent event data
     * @param authenticationCodec The codec to encode and decode authentication data
     * @param <T>                 The data type that is used in requests and events
     * @param <A>                 The data type that is send by the client for authentication
     * @return A new intercom server builder
     */
    public static <T, A> IntercomServer.Builder<T, A> server(IntercomCodec<T> codec, @Nullable IntercomCodec<A> authenticationCodec) {
        return new IntercomServer.Builder<>(codec, authenticationCodec);
    }

}
