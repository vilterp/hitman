package org.androidsofdeath.client.model;

import org.apache.http.HttpResponse;

import java.util.Arrays;

public class ApiException extends Exception {

    private HttpResponse response;
    private int[] expected;

    public ApiException(HttpResponse response, int... expected) {
        super(String.format("Unexpected status code: %d %s (expected %s)",
                response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(),
                Arrays.toString(expected)));
        this.response = response;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public int[] getExpected() {
        return expected;
    }

}
