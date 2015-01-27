package com.integreight.onesheeld.shields.controller.utils;

import android.content.Context;

import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.utils.ConnectionDetector;
import com.loopj.android.http.AsyncHttpClient;
import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

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
        return httpClient;
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
        requests.put(id, request);
    }

    public EXECUTION_TYPE execute(int id, REQUEST_TYPE type) {
        if (!ConnectionDetector.isConnectingToInternet(context))
            return EXECUTION_TYPE.NO_INTERNET;
        InternetRequest request = requests.get(id);
        if (request == null)
            return EXECUTION_TYPE.REQUEST_NOT_FOUND;
        if (request.getStatus() == InternetRequest.REQUEST_STATUS.SENT || request.getStatus() == InternetRequest.REQUEST_STATUS.CALLED)
            return EXECUTION_TYPE.ALREADY_EXECUTING;
        if (request.getUrl() == null || request.getUrl().trim().length() == 0)
            return EXECUTION_TYPE.NO_URL;
        if (request.getRegisteredCallbacks() == null || request.getRegisteredCallbacks().size() == 0)
            return EXECUTION_TYPE.NO_CALLBACKS;
        switch (type) {
            case GET:
                httpClient.get(context, request.getUrl(), request.getHeaders(), request.getParams(), request.getCallback());
                break;
            case POST:
                httpClient.post(context, request.getUrl(), request.getHeaders(), request.getParams(), request.getContentType(), request.getCallback());
                break;
            case PUT:
                httpClient.put(context, request.getUrl(), request.getParams(), request.getCallback());
                break;
            case DELETE:
                httpClient.delete(context, request.getUrl(), request.getHeaders(), request.getParams(), request.getCallback());
                break;
        }
        getRequest(id).setStatus(InternetRequest.REQUEST_STATUS.CALLED);
        return EXECUTION_TYPE.SUCCESSFUL;
    }

    public DB getCachDB() {
        return cachDB;
    }

    public void disponseRequest(int id) {
        if (requests.contains(id)) {
            requests.get(id).ignoreResponse();
            requests.remove(id);
        }
        try {
            cachDB.del(id + "");
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public enum EXECUTION_TYPE {
        NO_INTERNET, SUCCESSFUL, REQUEST_NOT_FOUND, ALREADY_EXECUTING, NO_URL, NO_CALLBACKS
    }

    public enum REQUEST_TYPE {
        GET, POST, DELETE, PUT
    }

}
