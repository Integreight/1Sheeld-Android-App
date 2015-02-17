package com.integreight.onesheeld.shields.controller;

import android.accounts.NetworkErrorException;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Enumeration;

public class InternetShield extends
        ControllerParent<ControllerParent<InternetShield>> {
    private static final byte SHIELD_ID = UIShield.INTERNET_SHIELD.id;

    /////////
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
    private static final byte GET_JSON_RESPONSE = (byte) 0x14;

    ////// SENT FRAMES
    private static final byte ON_SUCCESS = (byte) 0x01;
    private static final byte ON_FAILURE = (byte) 0x02;
    private static final byte ON_START = (byte) 0x03;
    private static final byte ON_PROGRESS = (byte) 0x04;
    private static final byte ON_FINISH = (byte) 0x05;
    // Internet
    private static final byte ON_ERROR = (byte) 0x06;
    /// Response

    private static final byte RESPONSE_ON_ERROR = (byte) 0x08;
    private static final byte RESPONSE_JSON = (byte) 0x0A;

    private ShieldFrame frame;
    int i = 0;
    private final String[] locs = new String[]{"Egypt", "London", "NYC", "Egypt", "Al-Qanater"};

    public void addRequest() {
        i = i + 1;
        final InternetRequest request = new InternetRequest();
        request.setUrl("http://api.openweathermap.org/data/2.5/weather?q=" + locs[(i - 1) % 5]);
        request.setId(i);
        request.addRegisteredCallbacks(InternetRequest.CALLBACK.ON_SUCCESS, InternetRequest.CALLBACK.ON_PROGRESS, InternetRequest.CALLBACK.ON_FAILURE);
        request.setCallback(new CallBack() {
            @Override
            public void onStart(int requestID) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody, int requestID) {
                try {
                    String str = new String(responseBody, "UTF-8");
                    Log.d("res", str);
                    InternetResponse response = InternetManager.getInstance().getRequest(i).getResponse();
                    ArrayList<InternetResponse.JsonNode> nodes = new ArrayList<>();
                    nodes.add(new InternetResponse.JsonNode(InternetResponse.JsonNode.NODE_DATA_TYPE.OBJECT, "weather"));
                    nodes.add(new InternetResponse.JsonNode(InternetResponse.JsonNode.NODE_DATA_TYPE.ARRAY, 0));
                    nodes.add(new InternetResponse.JsonNode(InternetResponse.JsonNode.NODE_DATA_TYPE.OBJECT, "main"));
                    if (response != null) {
                        String jsonRes = response.getValueOf(new JSONObject(new String(str)), nodes);
                        Log.d("res", jsonRes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(int requestID) {
                System.out.print(requestID + "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error, int requestID) {
                System.out.print(requestID + "");
            }

            @Override
            public void onProgress(int bytesWritten, int totalSize, int requestID) {
                System.out.print(requestID + "");
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

    public interface CallBack {
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody, int RequestID);

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error, int RequestID);

        public void onFinish(int requestID);

        public void onStart(int requestID);

        public void onProgress(int bytesWritten, int totalSize, int RequestID);
    }

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.INTERNET_SHIELD.getId()) {
            int requestID = 0;
            switch (frame.getFunctionId()) {
                case NEW_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetRequest request = new InternetRequest();
                    request.setId(requestID);
                    request.setUrl(frame.getArgumentAsString(1));
                    request.setCallback(new CallBack() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_SUCCESS);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, statusCode);//
                            frame1.addIntegerArgument(4, false, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null) {
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                                sendShieldFrame(frame1);
                            } else
                                onFailure(statusCode, headers, responseBody, new NetworkErrorException(), requestID);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_FAILURE);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, statusCode);//
                            frame1.addIntegerArgument(4, false, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null)
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                            sendShieldFrame(frame1);
                        }

                        @Override
                        public void onFinish(int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_FINISH);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            sendShieldFrame(frame1);
                        }

                        @Override
                        public void onStart(int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_START);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            sendShieldFrame(frame1);
                        }

                        @Override
                        public void onProgress(int bytesWritten, int totalSize, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_PROGRESS);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, bytesWritten);
                            frame1.addIntegerArgument(2, false, totalSize);
                            sendShieldFrame(frame1);
                        }
                    });
                    InternetManager.getInstance().putRequest(requestID, request);
                    break;
                case SET_URL:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setUrl("");
                    break;
                case ADD_HEADER:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).addHeader(frame.getArgumentAsString(1), frame.getArgumentAsString(2));
                    break;
                case ADD_PARAM:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).addParam(frame.getArgumentAsString(1), frame.getArgumentAsString(2));
                    break;
                case DEL_ALL_HEADERS:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().getRequest(requestID).removeAllHeaders();
                    break;
                case DEL_ALL_PARAMS:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().getRequest(requestID).removeAllParams();
                    break;
                case SET_CONTENT_TYPE:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setContentType(frame.getArgumentAsString(1));
                    break;
                case IGNORE_RESPONSE:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).ignoreResponse();
                    break;
                //////// INTERNET
                case GET_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    byte callbacks = frame.getArgument(1)[0];
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        int j = 0;
                        for (InternetRequest.CALLBACK callback : InternetRequest.CALLBACK.values()) {
                            if (BitsUtils.isBitSet(callbacks, j))
                                InternetManager.getInstance().getRequest(requestID).addRegisteredCallbacks(callback);
                            j++;
                        }
                    }
                    InternetManager.EXECUTION_TYPE getExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.GET);
                    if (getExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                        ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_ERROR);
                        frame1.addIntegerArgument(1, false, getExecutionType.value);///0=id
                        sendShieldFrame(frame1);
                    }
                    break;
                case POST_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    byte callbacks1 = frame.getArgument(1)[0];
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        int j = 0;
                        for (InternetRequest.CALLBACK callback : InternetRequest.CALLBACK.values()) {
                            if (BitsUtils.isBitSet(callbacks1, j))
                                InternetManager.getInstance().getRequest(requestID).addRegisteredCallbacks(callback);
                            j++;
                        }
                    }
                    InternetManager.EXECUTION_TYPE postExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.POST);
                    if (postExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                        ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_ERROR);
                        frame1.addIntegerArgument(1, false, postExecutionType.value);///0=id
                        sendShieldFrame(frame1);
                    }
                    break;
                case PUT_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    byte callbacks2 = frame.getArgument(1)[0];
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        int j = 0;
                        for (InternetRequest.CALLBACK callback : InternetRequest.CALLBACK.values()) {
                            if (BitsUtils.isBitSet(callbacks2, j))
                                InternetManager.getInstance().getRequest(requestID).addRegisteredCallbacks(callback);
                            j++;
                        }
                    }
                    InternetManager.EXECUTION_TYPE putExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.PUT);
                    if (putExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                        ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_ERROR);
                        frame1.addIntegerArgument(1, false, putExecutionType.value);///0=id
                        sendShieldFrame(frame1);
                    }
                    break;
                case DELETE_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    byte callbacks3 = frame.getArgument(1)[0];
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        int j = 0;
                        for (InternetRequest.CALLBACK callback : InternetRequest.CALLBACK.values()) {
                            if (BitsUtils.isBitSet(callbacks3, j))
                                InternetManager.getInstance().getRequest(requestID).addRegisteredCallbacks(callback);
                            j++;
                        }
                    }
                    InternetManager.EXECUTION_TYPE deleteExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.DELETE);
                    if (deleteExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                        ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, ON_ERROR);
                        frame1.addIntegerArgument(1, false, deleteExecutionType.value);///0=id
                        sendShieldFrame(frame1);
                    }
                    break;
                case CANCEL_ALL_REQUESTS:
                    InternetManager.getInstance().cancelAllRequests();
                    break;
                case SET_BASIC_AUTH:
                    InternetManager.getInstance().setBasicAuth(new Pair<>(frame.getArgumentAsString(0), frame.getArgumentAsString(1)));
                    break;
                case CLEAR_BASIC_AUTH:
                    InternetManager.getInstance().clearBasicAuth();
                    break;
                case SET_DEFAULT_MAX_RESPONSE_BYTES_COUNT:
                    InternetManager.getInstance().setMaxSentBytes(frame.getArgumentAsInteger(0));
                    break;
                /////// RESPONSE
                case DISPOSE:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().disponseResponse(requestID);
                    break;
                case GET_NEXT_BYTES:
                    requestID = frame.getArgumentAsInteger(0);
                    ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, RESPONSE_ON_ERROR);
                    frame1.addIntegerArgument(2, false, requestID);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            InternetResponse.ResponseBodyBytes bodyBytes = response.getBytes(frame.getArgumentAsInteger(1), frame.getArgumentAsInteger(2));
                            if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.NOT_ENOUGH_BYTES) {
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    frame1.addArgument(bodyBytes.getArray());
                                    frame1.addIntegerArgument(1, false, 3);
                                    sendShieldFrame(frame1);
                                }
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_GREATER_THAN_LENGTH || bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_LESS_THAN_0) {
                                frame1.addIntegerArgument(1, false, 0);
                                sendShieldFrame(frame1);
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.COUNT_LESS_THAN_0) {
                                frame1.addIntegerArgument(1, false, 5);
                                sendShieldFrame(frame1);
                            } else {
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    frame1.addArgument(bodyBytes.getArray());
                                    sendShieldFrame(frame1);
                                }
                            }
                        } else {//no response
                            frame1.addIntegerArgument(1, false, 4);
                            sendShieldFrame(frame1);
                        }
                    } else// no request
                    {
                        frame1.addIntegerArgument(1, false, 1);
                        sendShieldFrame(frame1);
                    }
                    break;
                case GET_HEADER:
                    requestID = frame.getArgumentAsInteger(0);
                    String key = frame.getArgumentAsString(1);
                    ShieldFrame frame2 = new ShieldFrame(SHIELD_ID, (byte) 0x08);
                    frame2.addIntegerArgument(1, false, requestID);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            if (response.getHeaders() == null || response.getHeaders().get(key) == null) {
                                frame2.addIntegerArgument(1, false, 2);
                                sendShieldFrame(frame2);
                            } else {
                                frame2.addStringArgument(key);
                                frame2.addStringArgument(response.getHeaders().get(key));
                                sendShieldFrame(frame2);
                            }
                        } else {//no response
                            frame2.addIntegerArgument(1, false, 4);
                            sendShieldFrame(frame2);
                        }
                    } else// no request
                    {
                        frame2.addIntegerArgument(1, false, 1);
                        sendShieldFrame(frame2);
                    }
                    break;
                case GET_JSON_RESPONSE:
                    requestID = frame.getArgumentAsInteger(0);
                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE_JSON);
                    frameJson.addIntegerArgument(1, false, requestID);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            final int jsonNodesTree = frame.getArgumentAsInteger(0, 1);
                            final ArrayList<InternetResponse.JsonNode> jsonNodes = response.getNodes(frame);
                        } else {//no response
                            frameJson.addIntegerArgument(1, false, 4);
                            sendShieldFrame(frameJson);
                        }
                    } else// no request
                    {
                        frameJson.addIntegerArgument(1, false, 1);
                        sendShieldFrame(frameJson);
                    }
                    break;
                default:
                    break;
            }
            if (InternetManager.getInstance().getUiCallback() != null)
                InternetManager.getInstance().getUiCallback().onStart();
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
            req.setContentType(mainReq.getContentType());
            req.setParams(mainReq.getParamsAsMap());
            req.setHeaders(mainReq.getHeadersAsMap());
            req.setStatus(mainReq.getStatus());
            ArrayList<Pair<String, String>> children = new ArrayList<>();
            children.add(new Pair<>("URL", req.getUrl()));
            InternetResponse res = mainReq.getResponse();
            if (mainReq.getStatus() == InternetRequest.REQUEST_STATUS.EXECUTED && res != null && res.getResponseBody() != null && res.getResponseBody().length > 0) {
                String response = new String(res.getResponseBody());
                children.add(new Pair<>("Response", response != null && response.length() >= 30 ? response.substring(0, 30) + "..." : response));
            }
            children.add(new Pair<>("Content Type", req.getContentType() != null && req.getContentType().trim().length() > 0 ? req.getContentType() : "No Content Type"));
            if (InternetManager.getInstance().getBasicAuth() != null)
                children.add(new Pair<>("Authentication", InternetManager.getInstance().getBasicAuth().first + " : " + InternetManager.getInstance().getBasicAuth().second));
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
