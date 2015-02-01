package com.integreight.onesheeld.shields.controller;

import android.app.Activity;
import android.util.Pair;

import com.integreight.firmatabluetooth.ShieldFrame;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.model.InternetResponse;
import com.integreight.onesheeld.model.InternetUiRequest;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.InternetManager;
import com.integreight.onesheeld.utils.BitsUtils;
import com.integreight.onesheeld.utils.Log;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.snappydb.SnappydbException;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Enumeration;

public class InternetShield extends
        ControllerParent<ControllerParent<InternetShield>> {
    private static final byte NEW_REQUEST = (byte) 0x01;
    private static final byte SET_URL = (byte) 0x02;
    private static final byte ADD_HEADER = (byte) 0x03;
    private static final byte ADD_PARAM = (byte) 0x04;
    private static final byte DEL_ALL_HEADERS = (byte) 0x05;
    private static final byte DEL_ALL_PARAMS = (byte) 0x06;
    private static final byte SET_CONTENT_TYPE = (byte) 0x07;
    private static final byte IGNORE_RESPONSE = (byte) 0x08;

    //////// INTERNET
    private static final byte GET_REQUEST = (byte) 0x09;
    private static final byte POST_REQUEST = (byte) 0x0A;
    private static final byte PUT_REQUEST = (byte) 0x0B;
    private static final byte DELETE_REQUEST = (byte) 0x0C;
    private static final byte CANCEL_ALL_REQUESTS = (byte) 0x0D;
    private static final byte SET_BASIC_AUTH = (byte) 0x0E;
    private static final byte CLEAR_BASIC_AUTH = (byte) 0x0F;
    private static final byte SET_DEFAULT_MAX_RESPONSE_BYTES_COUNT = (byte) 0x10;

    /////// RESPONSE
    private static final byte DISPOSE = (byte) 0x11;
    private static final byte GET_NEXT_BYTES = (byte) 0x12;
    private static final byte GET_HEADER = (byte) 0x13;

    private ShieldFrame frame;
    int i = 0;

    public void addRequest() {
        i = i + 1;
        InternetRequest request = new InternetRequest();
        request.setUrl("http://www.twitter.com/");
        request.setId(i);
        request.addRegisteredCallbacks(InternetRequest.CALLBACK.ON_SUCCESS, InternetRequest.CALLBACK.ON_PROGRESS);
        request.setCallback(new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Log.d("res", str);
                    InternetResponse response = InternetManager.getInstance().getRequest(i).getResponse();
                    if (response != null)
                        Log.d("res", response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                super.onProgress(bytesWritten, totalSize);
            }
        });
        InternetManager.getInstance().putRequest(i, request);
        InternetManager.getInstance().execute(i, InternetManager.REQUEST_TYPE.GET);
    }

    @Override
    public ControllerParent<ControllerParent<InternetShield>> init(String tag) {
        // TODO Auto-generated method stub\
        System.out.print("");
        try {
            InternetManager.getInstance();
            if (InternetManager.getInstance().getCachDB() == null || !InternetManager.getInstance().getCachDB().isOpen())
                InternetManager.getInstance().init(activity.getApplicationContext());
            else {
                if (!InternetManager.getInstance().getCachDB().isOpen()) {
                    InternetManager.getInstance().close();
                    InternetManager.resetInstance().init(activity.getApplicationContext());
                }
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return super.init(tag);
    }

    public InternetShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public InternetShield() {
        super();
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.INTERNET_SHIELD.getId()) {
            int requestID = BitsUtils.byteArrayToInt(frame.getArgument(0));
            switch (frame.getFunctionId()) {
                case NEW_REQUEST:
                    break;
                case SET_URL:
                    InternetManager.getInstance().getRequest(requestID).setUrl("");
                    break;
                case ADD_HEADER:
                    break;
                case ADD_PARAM:
                    break;
                case DEL_ALL_HEADERS:
                    InternetManager.getInstance().getRequest(requestID).removeAllHeaders();
                    break;
                case DEL_ALL_PARAMS:
                    InternetManager.getInstance().getRequest(requestID).removeAllParams();
                    break;
                case SET_CONTENT_TYPE:
                    InternetManager.getInstance().getRequest(requestID).setContentType("");
                    break;
                case IGNORE_RESPONSE:
                    InternetManager.getInstance().getRequest(requestID).ignoreResponse();
                    break;
                //////// INTERNET
                case GET_REQUEST:
                    InternetManager.EXECUTION_TYPE getExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.GET);
                    if (getExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {

                    }
                    break;
                case POST_REQUEST:
                    InternetManager.EXECUTION_TYPE postExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.POST);
                    if (postExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {

                    }
                    break;
                case PUT_REQUEST:
                    InternetManager.EXECUTION_TYPE putExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.PUT);
                    if (putExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {

                    }
                    break;
                case DELETE_REQUEST:
                    InternetManager.EXECUTION_TYPE deleteExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.DELETE);
                    if (deleteExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {

                    }
                    break;
                case CANCEL_ALL_REQUESTS:
                    InternetManager.getInstance().cancelAllRequests();
                    break;
                case SET_BASIC_AUTH:
                    break;
                case CLEAR_BASIC_AUTH:
                    InternetManager.getInstance().getRequest(requestID).clearAuth();
                    break;
                case SET_DEFAULT_MAX_RESPONSE_BYTES_COUNT:
                    break;
                /////// RESPONSE
                case DISPOSE:
                    InternetManager.getInstance().disponseResponse(requestID);
                    break;
                case GET_NEXT_BYTES:
                    InternetManager.getInstance().getRequest(requestID).getResponse().getBytes(0, 0);
                    break;
                case GET_HEADER:
                    InternetManager.getInstance().getRequest(requestID).getResponse().getHeaders().get("");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void reset() {
        try {
            InternetManager.getInstance().close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }

    public void setUiCallback(AsyncHttpResponseHandler callback) {
        InternetManager.getInstance().setUiCallback(callback);
    }

    public ArrayList<InternetUiRequest> getUiRequests() {
        ArrayList<InternetUiRequest> requestsUI = new ArrayList<>();
        Enumeration e = InternetManager.getInstance().getRequests().keys();
        while (e.hasMoreElements()) {
            Integer i = (Integer) e.nextElement();
            InternetRequest mainReq = InternetManager.getInstance().getRequest(i);
            InternetUiRequest req = new InternetUiRequest();
            req.setId(mainReq.getId());
            req.setUrl(mainReq.getUrl());
            if (mainReq.getAuth() != null)
                req.setAuth(mainReq.getAuth().first, mainReq.getAuth().second);
            req.setContentType(mainReq.getContentType());
            req.setParams(mainReq.getParamsAsMap());
            req.setHeaders(mainReq.getParamsAsMap());
            req.setStatus(mainReq.getStatus());
            ArrayList<Pair<String, String>> children = new ArrayList<>();
            children.add(new Pair<>("URL", req.getUrl()));
            InternetResponse res = mainReq.getResponse();
            if (mainReq.getStatus() == InternetRequest.REQUEST_STATUS.EXECUTED && res != null && res.getResponseBody() != null && res.getResponseBody().length > 0) {
                String response = new String(res.getResponseBody());
                children.add(new Pair<>("Response", response != null && response.length() >= 30 ? response.substring(0, 30) + "..." : response));
            }
            children.add(new Pair<>("Content Type", req.getContentType() != null && req.getContentType().trim().length() > 0 ? req.getContentType() : "No Content Type"));
            if (req.getAuth() != null)
                children.add(new Pair<>("Authentication", req.getAuth().first + " : " + req.getAuth().second));
            else
                children.add(new Pair<>("Authentication", "No Authentication"));
            String params = "No Parameters";
            if (req.getParams() != null && req.getParamsAsMap().size() > 0) {
                params = "";
                for (String key : req.getParamsAsMap().keySet()) {
                    params += key + " : " + req.getParamsAsMap().get(key) + "\n";
                }
            }
            children.add(new Pair<>("Parameters", params));
            String headers = "No Headers";
            if (req.getHeaders() != null) {
                headers = "";
                for (Header header : req.getHeaders()) {
                    headers += header.getName() + " : " + header.getValue() + "\n";
                }
            }
            children.add(new Pair<>("Headers", headers));
            req.setUiChildren(children);
            requestsUI.add(req);
        }
        return requestsUI;
    }


}
