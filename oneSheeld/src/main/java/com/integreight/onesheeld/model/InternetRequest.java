package com.integreight.onesheeld.model;

import android.util.Pair;

import com.integreight.onesheeld.shields.controller.utils.InternetManager;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.snappydb.SnappydbException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Saad on 1/26/15.
 */
public class InternetRequest {
    private String url;
    private int id;
    private REQUEST_STATUS status;
    private AsyncHttpResponseHandler mCallback;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Pair<String, String> auth;
    private String contentType;
    private boolean isIgnored = false;
    private ArrayList<String> registeredCallbacks;

    public InternetRequest() {
        status = REQUEST_STATUS.IN_QUEUE;
        auth = null;
        mCallback = null;
        registeredCallbacks = new ArrayList<>();
        headers = new HashMap<>();
        params = new HashMap<>();
        setCallback(new AsyncHttpResponseHandler());
    }

    public InternetRequest(String url, int id, AsyncHttpResponseHandler callback) {
        this();
        this.url = url;
        this.id = id;
        this.mCallback = callback;
        setCallback(new AsyncHttpResponseHandler());
        this.headers = new HashMap<>();
        this.params = new HashMap<>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public REQUEST_STATUS getStatus() {
        return status;
    }

    public void setStatus(REQUEST_STATUS status) {
        this.status = status;
    }

    public AsyncHttpResponseHandler getCallback() {
        return mCallback;
    }

    public void setCallback(final AsyncHttpResponseHandler callback) {
        this.mCallback = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                status = REQUEST_STATUS.SENT;
//                if (mCallback != null)
//                    mCallback.onStart();
                if (!isIgnored && registeredCallbacks.contains(CALLBACK.ON_START.name()))
                    callback.onStart();
                isIgnored = false;
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                status = REQUEST_STATUS.EXECUTED;
                setResponse(new InternetResponse(responseBody, statusCode, InternetResponse.RESPONSE_STATUS.SUCCESSFUL, headers));
//                if (mCallback != null)
//                    mCallback.onSuccess(statusCode, headers, responseBody);
                if (!isIgnored && registeredCallbacks.contains(CALLBACK.ON_SUCCESS.name()))
                    callback.onSuccess(statusCode, headers, responseBody);
                isIgnored = false;
                super.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                status = REQUEST_STATUS.EXECUTED;
                setResponse(new InternetResponse(responseBody, statusCode, InternetResponse.RESPONSE_STATUS.FAILURE, headers));
//                if (mCallback != null)
//                    mCallback.onFailure(statusCode, headers, responseBody, error);
                if (!isIgnored && registeredCallbacks.contains(CALLBACK.ON_FAILURE.name()))
                    callback.onFailure(statusCode, headers, responseBody, error);
                isIgnored = false;
                super.onFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onFinish() {
                status = REQUEST_STATUS.EXECUTED;
//                if (mCallback != null)
//                    mCallback.onFinish();
                if (!isIgnored && registeredCallbacks.contains(CALLBACK.ON_FINISH.name()))
                    callback.onFinish();
                isIgnored = false;
                super.onFinish();
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
//                if (mCallback != null)
//                    mCallback.onProgress(bytesWritten, totalSize);
                if (!isIgnored && registeredCallbacks.contains(CALLBACK.ON_PROGRESS.name()))
                    callback.onProgress(bytesWritten, totalSize);
                isIgnored = false;
                super.onProgress(bytesWritten, totalSize);
            }
        };
    }

    public void updateCallback(AsyncHttpResponseHandler mCallback) {
        this.mCallback = mCallback;
    }

    public InternetResponse getResponse() {
        try {
            return InternetManager.getInstance().getCachDB().get(id + "", InternetResponse.class);
        } catch (SnappydbException e) {
            return null;
        }
    }

    public void setResponse(InternetResponse response) {
        try {
            InternetManager.getInstance().getCachDB().put(id + "", response);
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void removeHeader(String key) {
        if (headers.get(key) != null)
            headers.remove(key);
    }

    public void removeAllHeaders() {
        if (headers != null)
            headers.clear();
        headers = new HashMap<>();
    }

    public void addParam(String key, String value) {
        headers.put(key, value);
    }

    public void removeParam(String key) {
        if (headers.get(key) != null)
            headers.remove(key);
    }

    public void removeAllParams() {
        params = new HashMap<>();
    }

    public Header[] getHeaders() {
        if (headers == null || headers.size() == 0)
            return null;
        Header[] returnedHeaders = new Header[headers.keySet().size()];
        int i = 0;
        for (final String key : headers.keySet()) {
            returnedHeaders[i] = new Header() {
                @Override
                public String getName() {
                    return key;
                }

                @Override
                public String getValue() {
                    return headers.get(key);
                }

                @Override
                public HeaderElement[] getElements() throws ParseException {
                    return new HeaderElement[0];
                }
            };
            i++;
        }
        return returnedHeaders;
    }

    public RequestParams getParams() {
        RequestParams paramsI = new RequestParams();
        for (final String key : params.keySet()) {
            paramsI.add(key, params.get(key));
        }
        return paramsI;
    }

    public HashMap<String, String> getParamsAsMap() {
        return new HashMap<>(params);
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Pair<String, String> getAuth() {
        return auth;
    }

    public void setAuth(String userName, String passWord) {
        this.auth = new Pair<>(userName, passWord);
    }

    public void clearAuth() {
        this.auth = null;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void ignoreResponse() {
        isIgnored = true;
    }

    public void addRegisteredCallbacks(CALLBACK... callbacks) {
        if (registeredCallbacks == null)
            registeredCallbacks = new ArrayList<>();
        for (CALLBACK callback : callbacks)
            registeredCallbacks.add(callback.name());
    }

    public ArrayList<String> getRegisteredCallbacks() {
        return registeredCallbacks;
    }

    public enum CALLBACK {
        ON_START, ON_FINISH, ON_SUCCESS, ON_FAILURE, ON_PROGRESS
    }

    public static enum REQUEST_STATUS {
        CALLED, SENT, IN_QUEUE, EXECUTED
    }

}
