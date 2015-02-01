package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;

import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.apache.http.Header;

import java.util.Hashtable;

/**
 * Created by Saad on 1/26/15.
 */
public class InternetManager {
    private static InternetManager ourInstance;
    private AsyncHttpClient httpClient;
    private Hashtable<Integer, InternetRequest> requests;
    private DB cachDB;
    private Context context;
    private AsyncHttpResponseHandler uiCallback;
    private String contentType = "";
    private int maxSentBytes = 255;


    private InternetManager() {
        httpClient = new AsyncHttpClient();
        requests = new Hashtable<>();
    }

    public static InternetManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new InternetManager();
        }
        return ourInstance;
    }

    public static InternetManager resetInstance() {
        try {
            getInstance().close();
        } catch (Exception e) {
        }
        ourInstance = new InternetManager();
        return ourInstance;
    }

    public void init(Context context) throws SnappydbException {
        this.context = context;
        cachDB = DBFactory.open(context);
    }

    public void close() throws SnappydbException {
        if (httpClient != null)
            httpClient.cancelRequests(context, true);
        if (requests != null)
            requests.clear();
        if (cachDB != null && cachDB.isOpen()) {
            cachDB.close();
            cachDB.destroy();
        }
        context = null;
    }

    public void cancelAllRequests() {
        if (httpClient != null)
            httpClient.cancelRequests(context, true);
    }

    public AsyncHttpClient getHttpClient() {
        if (httpClient == null)
            httpClient = new AsyncHttpClient();
        return httpClient;
    }

    public Hashtable<Integer, InternetRequest> getRequests() {
        return requests;
    }

    public InternetRequest getRequest(int id) {
        InternetRequest request = requests.get(id);
        if (request != null)
            return request;
        else {
            putRequest(id, new InternetRequest());
            return getRequest(id);
        }
    }

    public void putRequest(int id, final InternetRequest request) {
        request.setContentType(contentType);
        requests.put(id, request);
        if (uiCallback != null)
            uiCallback.onStart();
    }

    public AsyncHttpResponseHandler getUiCallback() {
        return uiCallback;
    }

    public void setUiCallback(AsyncHttpResponseHandler uiCallback) {
        this.uiCallback = uiCallback;
    }

    public EXECUTION_TYPE execute(int id, REQUEST_TYPE type) {
        if (!ConnectionDetector.isConnectingToInternet(context))
            return EXECUTION_TYPE.NO_INTERNET;
        final InternetRequest request = requests.get(id);
        if (request == null)
            return EXECUTION_TYPE.REQUEST_NOT_FOUND;
        if (request.getStatus() == InternetRequest.REQUEST_STATUS.SENT || request.getStatus() == InternetRequest.REQUEST_STATUS.CALLED)
            return EXECUTION_TYPE.ALREADY_EXECUTING;
        if (request.getUrl() == null || request.getUrl().trim().length() == 0)
            return EXECUTION_TYPE.NO_URL;
//        if (request.getRegisteredCallbacks() == null || request.getRegisteredCallbacks().size() == 0)
//            return EXECUTION_TYPE.NO_CALLBACKS;
        final AsyncHttpResponseHandler withUiCallBack = new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                if (request.getCallback() != null)
                    request.getCallback().onStart();
                if (getUiCallback() != null)
                    getUiCallback().onStart();
                super.onStart();
            }

            @Override
            public void onFinish() {
                if (request.getCallback() != null)
                    request.getCallback().onFinish();
                if (getUiCallback() != null)
                    getUiCallback().onFinish();
                super.onFinish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (request.getCallback() != null)
                    request.getCallback().onFailure(statusCode, headers, responseBody, error);
                super.onFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if (request.getCallback() != null)
                    request.getCallback().onSuccess(statusCode, headers, responseBody);
                super.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                if (request.getCallback() != null)
                    request.getCallback().onProgress(bytesWritten, totalSize);
                super.onProgress(bytesWritten, totalSize);
            }

        };
        getHttpClient().clearBasicAuth();
        if (request.getAuth() != null && request.getAuth().first != null && request.getAuth().first.trim().length() > 0)
            getHttpClient().setBasicAuth(request.getAuth().first, request.getAuth().second);

        switch (type) {
            case GET:
                getHttpClient().get(context, request.getUrl(), request.getHeaders(), request.getParams(), withUiCallBack);
                break;
            case POST:
                getHttpClient().post(context, request.getUrl(), request.getHeaders(), request.getParams(), request.getContentType(), withUiCallBack);
                break;
            case PUT:
                getHttpClient().put(context, request.getUrl(), request.getParams(), withUiCallBack);
                break;
            case DELETE:
                getHttpClient().delete(context, request.getUrl(), request.getHeaders(), request.getParams(), withUiCallBack);
                break;
        }
        getRequest(id).setStatus(InternetRequest.REQUEST_STATUS.CALLED);
        return EXECUTION_TYPE.SUCCESSFUL;
    }

    public DB getCachDB() {
        return cachDB;
    }

    public void disponseResponse(int id) {
//        if (requests.contains(id)) {
//            requests.get(id).ignoreResponse();
//            requests.remove(id);
//        }
        try {
            cachDB.del(id + "");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getMaxSentBytes() {
        return maxSentBytes;
    }

    public void setMaxSentBytes(int maxSentBytes) {
        this.maxSentBytes = maxSentBytes;
    }

    public enum EXECUTION_TYPE {
        NO_INTERNET, SUCCESSFUL, REQUEST_NOT_FOUND, ALREADY_EXECUTING, NO_URL, NO_CALLBACKS
    }

    public enum REQUEST_TYPE {
        GET, POST, DELETE, PUT
    }

}
