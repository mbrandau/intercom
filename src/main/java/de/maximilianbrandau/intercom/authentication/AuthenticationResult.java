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
 *
 */
public class AuthenticationResult {

    private final String error;

    private AuthenticationResult(String error) {
        this.error = error;
    }

    /**
     * Creates a new successful {@link AuthenticationResult}
     *
     * @return A successful AuthenticationResult
     */
    public static AuthenticationResult success() {
        return new AuthenticationResult(null);
    }

    /**
     * Creates a new failed {@link AuthenticationResult}
     *
     * @param error The error message that states why the authentication failed. If set to <code>null</code>, @see isSuccess will return true.
     * @return A failed AuthenticationResult
     */
    public static AuthenticationResult failure(String error) {
        return new AuthenticationResult(error);
    }

    /**
     * Checks if the authentication was successful
     *
     * @return Returns true if the authentication was successful
     */
    public boolean isSuccess() {
        return this.error == null;
    }

    /**
     * Gets the error message that was set on a failed authentication
     *
     * @return The error message if the authentication did not succeed. Returns <code>null</code> if the authentication was successful.
     */
    public String getError() {
        return error;
    }

}
