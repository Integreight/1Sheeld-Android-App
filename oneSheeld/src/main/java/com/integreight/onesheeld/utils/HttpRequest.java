package com.integreight.onesheeld.utils;

import com.loopj.android.http.AsyncHttpClient;

public class HttpRequest {
    private static AsyncHttpClient httpRequest;

    private HttpRequest() {
    }

    public static AsyncHttpClient getInstance() {
        if (httpRequest == null)
            httpRequest = new AsyncHttpClient();
        return httpRequest;
    }
}
