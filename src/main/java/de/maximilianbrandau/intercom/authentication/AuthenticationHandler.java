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

package de.maximilianbrandau.intercom.authentication;

/**
 * Used by the {@link de.maximilianbrandau.intercom.server.IntercomServer} to authenticate the {@link de.maximilianbrandau.intercom.client.IntercomClient}
 *
 * @param <A> Data type of the authentication data
 */
public interface AuthenticationHandler<A> {

    /**
     * Authenticated the client
     * <p>
     * Given that the authentication data of type {@link String} represents a password, you can create an {@link AuthenticationResult} like this:
     * <pre>
     * {@code
     * return authenticationData.equals("correctPassword")? AuthenticationResult.success() : AuthenticationResult.failure("Passwords don't match");
     * }
     * </pre>
     * Note that the error is send to the client, so you might want to avoid sending the correct password in the message.
     *
     * @param authenticationData Authentication data supplied by the {@link de.maximilianbrandau.intercom.client.IntercomClient}
     * @return An {@link AuthenticationResult}
     */
    AuthenticationResult authenticate(A authenticationData);

}
