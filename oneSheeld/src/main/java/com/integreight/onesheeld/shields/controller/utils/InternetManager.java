package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;
import android.util.Pair;

import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.shields.controller.InternetShield;
import com.integreight.onesheeld.utils.BitsUtils;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import cz.msebera.android.httpclient.client.params.ClientPNames;

/**
 * Created by Saad on 1/26/15.
 * <p/>
 * SingleTone class for Internet Shield, has most of the logic for internet
 */
public class InternetManager {
    private static InternetManager ourInstance;
    private AsyncHttpClient httpClient;
    private Hashtable<Integer, InternetRequest> requests;
    private DB cachDB;
    private Context context;
    private AsyncHttpResponseHandler uiCallback;
    private int maxSentBytes = 64;
    private Pair<String, String> basicAuth;


    private InternetManager() {
        httpClient = new AsyncHttpClient(true, 80, 443);
        requests = new Hashtable<>();
        basicAuth = null;
    }

    public static synchronized InternetManager getInstance() {
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

    public void setContext(Context context) {
        this.context = context;
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
        if (requests != null && requests.size() > 0) {
            Enumeration e = requests.keys();
            while (e.hasMoreElements()) {
                Integer i = (Integer) e.nextElement();
                requests.get(i).setCancelled();
            }
        }
        if (httpClient != null)
            httpClient.cancelRequests(context, true);
    }

    public AsyncHttpClient getHttpClient() {
        if (httpClient == null)
            httpClient = new AsyncHttpClient(true, 80, 443);
        httpClient.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        return httpClient;
    }

    public Hashtable<Integer, InternetRequest> getRequests() {
        return requests;
    }

    public synchronized InternetRequest getRequest(int id) {
        InternetRequest request = requests.get(id);
        if (request != null)
            return request;
        else {
            return null;
        }
    }

    public synchronized void putRequest(int id, final InternetRequest request) {
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

    public EXECUTION_TYPE execute(int id, REQUEST_TYPE type, byte callbacks) throws UnsupportedEncodingException {
        final InternetRequest request = requests.get(id);
        if (request == null)
            return EXECUTION_TYPE.REQUEST_NOT_FOUND;
        if (!ConnectionDetector.isConnectingToInternet(context))
            return EXECUTION_TYPE.NO_INTERNET;
        if (request.getStatus() == InternetRequest.REQUEST_STATUS.SENT || request.getStatus() == InternetRequest.REQUEST_STATUS.CALLED)
            return EXECUTION_TYPE.ALREADY_EXECUTING;
        if (request.getUrl() == null || request.getUrl().trim().length() == 0)
            return EXECUTION_TYPE.NO_URL;
        if (request.getUrl().contains(" "))
            return EXECUTION_TYPE.URL_IS_WRONG;
//        if (request.getRegisteredCallbacks() == null || request.getRegisteredCallbacks().size() == 0)
//            return EXECUTION_TYPE.NO_CALLBACKS;
        // registering callback according excution request parameter
        if (InternetManager.getInstance().getRequest(id) != null) {
            int j = 0;
            for (InternetRequest.CALLBACK callback : InternetRequest.CALLBACK.values()) {
                if (BitsUtils.isBitSet(callbacks, j))
                    request.addRegisteredCallbacks(callback);
                j++;
            }
        }
        //update request after registering callback
        InternetManager.getInstance().putRequest(id, request);
        // using given callback for UI updates
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
            public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                if (request.getCallback() != null)
                    request.getCallback().onFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                if (request.getCallback() != null)
                    request.getCallback().onSuccess(statusCode, headers, responseBody);
            }

        };

        if (InternetManager.getInstance().getBasicAuth() != null && InternetManager.getInstance().getBasicAuth().first != null && InternetManager.getInstance().getBasicAuth().first.trim().length() > 0)
            getHttpClient().setBasicAuth(InternetManager.getInstance().getBasicAuth().first, InternetManager.getInstance().getBasicAuth().second);

        switch (type) {
            case GET:
                getHttpClient().get(context, request.getUrl(), request.getHeaders(), request.getParams(), withUiCallBack);
                break;
            case POST:
                if (request.getEntity() != null)
                    getHttpClient().post(context, request.getUrl(), request.getHeaders(), new cz.msebera.android.httpclient.entity.StringEntity(request.getEntity()), request.getContentType(), withUiCallBack);
                else if (request.getFileEntity() != null) {
                    try {
                        getHttpClient().post(context, request.getUrl(), request.getHeaders(), new cz.msebera.android.httpclient.entity.ByteArrayEntity(FileUtils.readFileToByteArray(new File(request.getFileEntity()))), request.getContentType(), withUiCallBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    getHttpClient().post(context, request.getUrl(), request.getHeaders(), request.getParams(), request.getContentType(), withUiCallBack);
                break;
            case PUT:
                if (request.getEntity() != null)
                    getHttpClient().put(context, request.getUrl(), request.getHeaders(), new cz.msebera.android.httpclient.entity.StringEntity(request.getEntity()), request.getContentType(), withUiCallBack);
                else if (request.getFileEntity() != null) {
                    try {
                        getHttpClient().post(context, request.getUrl(), request.getHeaders(), new cz.msebera.android.httpclient.entity.ByteArrayEntity(FileUtils.readFileToByteArray(new File(request.getFileEntity()))), request.getContentType(), withUiCallBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
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
        try {
            cachDB.del(id + "");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        if (uiCallback != null)
            uiCallback.onStart();
    }

    public int getMaxSentBytes() {
        return maxSentBytes;
    }

    public void setMaxSentBytes(int maxSentBytes) {
        this.maxSentBytes = maxSentBytes > 255 ? 255 : maxSentBytes;
    }

    public Pair<String, String> getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(Pair<String, String> basicAuth) {
        if (basicAuth != null && basicAuth.first != null && basicAuth.first.trim().length() > 0 && basicAuth.second != null && basicAuth.second.trim().length() > 0)
            this.basicAuth = new Pair<>(basicAuth.first, basicAuth.second);
        else
            this.basicAuth = null;
    }

    public void clearBasicAuth() {
        this.basicAuth = null;
    }

    public enum EXECUTION_TYPE {
        NO_INTERNET(InternetShield.INTERNET.NOT_CONNECTED_TO_NETWORK), SUCCESSFUL(-1), REQUEST_NOT_FOUND(InternetShield.INTERNET.REQUEST_CAN_NOT_BE_FOUND), ALREADY_EXECUTING(InternetShield.INTERNET.ALREADY_EXECUTING_REQUEST), NO_URL(InternetShield.INTERNET.URL_IS_NOT_FOUND), URL_IS_WRONG(InternetShield.INTERNET.URL_IS_WRONG);
        public int value = -1;

        private EXECUTION_TYPE(int value) {
            this.value = value;
        }
    }

    public enum REQUEST_TYPE {
        GET, POST, DELETE, PUT
    }

}
