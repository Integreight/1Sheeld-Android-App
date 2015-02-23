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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;

public class InternetShield extends
        ControllerParent<ControllerParent<InternetShield>> {
    private static final byte SHIELD_ID = UIShield.INTERNET_SHIELD.id;

    /////////
    private final static class REQUEST {
        public static final byte NEW_REQUEST = (byte) 0x01;
        public static final byte SET_URL = (byte) 0x02;
        public static final byte ADD_HEADER = (byte) 0x03;
        public static final byte ADD_PARAM = (byte) 0x04;
        public static final byte DEL_ALL_HEADERS = (byte) 0x05;
        public static final byte DEL_ALL_PARAMS = (byte) 0x06;
        public static final byte SET_CONTENT_TYPE = (byte) 0x07;
        public static final byte IGNORE_RESPONSE = (byte) 0x08;
        public static final byte ADD_HTTP_ENTITY = (byte) 0x15;
        public static final byte SET_ENCODING = (byte) 0x16;

        ////// SENT FRAMES FUNICTIONS IDs
        private static final byte ON_SUCCESS = (byte) 0x01;
        private static final byte ON_FAILURE = (byte) 0x02;
        private static final byte ON_START = (byte) 0x03;
        private static final byte ON_PROGRESS = (byte) 0x04;
        private static final byte ON_FINISH = (byte) 0x05;
    }

    //////// INTERNET
    public final static class INTERNET {
        public static final byte GET_REQUEST = (byte) 0x09;
        public static final byte POST_REQUEST = (byte) 0x0A;
        public static final byte PUT_REQUEST = (byte) 0x0B;
        public static final byte DELETE_REQUEST = (byte) 0x0C;
        public static final byte CANCEL_ALL_REQUESTS = (byte) 0x0D;
        public static final byte SET_BASIC_AUTH = (byte) 0x0E;
        public static final byte CLEAR_BASIC_AUTH = (byte) 0x0F;
        public static final byte SET_DEFAULT_MAX_RESPONSE_BYTES_COUNT = (byte) 0x10;
        //Error
        public static final byte ON_ERROR = (byte) 0x06; // funID
        //// Errors types
        public static final int REQUEST_CAN_NOT_BE_FOUND = 1;
        public static final int NOT_CONNECTED_TO_NETWORK = 0;
        public static final int URL_IS_NOT_FOUND = 2;
        public static final int ALREADY_EXECUTING_REQUEST = 3;
    }

    /////// RESPONSE
    private final static class RESPONSE {
        public static final byte DISPOSE = (byte) 0x11;
        public static final byte GET_NEXT_BYTES = (byte) 0x12;
        public static final byte GET_HEADER = (byte) 0x13;
        public static final byte GET_JSON_RESPONSE = (byte) 0x14;
        public static final byte GET_JSON_ARRAY_LENGTH = (byte) 0x17;


        ////// SENT FRAMES
        public static final byte SEND_GET_NEXT_BYTES = (byte) 0x09;
        public static final byte SEND_GET_HEADER = (byte) 0x07;
        public static final byte RESPONSE_JSON = (byte) 0x0A;
        public static final byte RESPONSE_JSON_ARRAY = (byte) 0x0B;

        /// ERROR
        public static final byte ON_ERROR = (byte) 0x08; //fun ID

        //errors type
        public static final int INDEX_OUT_OF_BOUNDS = 0;
        public static final int RESPONSE_CAN_NOT_BE_FOUND = 1;
        public static final int HEADER_CAN_NOT_BE_FOUND = 2;
        public static final int NO_ENOUGH_BYTES = 3;
        public static final int REQUEST_HAS_NO_RESPONSE = 4;
        public static final int SIZE_OF_REQUEST_CAN_NOT_BE_ZERO = 5;
        public static final int UNSUPPORTED_ENTITY_ENCODING = 6;
        public static final int JSON_KEYCHAIN_IS_WRONG = 7;
    }

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
        try {
            InternetManager.getInstance().execute(i, InternetManager.REQUEST_TYPE.GET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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

    int requestID = 0;

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.INTERNET_SHIELD.getId()) {
            switch (frame.getFunctionId()) {
                case REQUEST.NEW_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetRequest request = new InternetRequest();
                    request.setId(requestID);
                    request.setUrl(frame.getArgumentAsString(1));
                    request.setCallback(new CallBack() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_SUCCESS);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, statusCode);//
                            frame1.addIntegerArgument(4, false, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null) {
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                                sendShieldFrame(frame1,true);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_FAILURE);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, statusCode);//
                            frame1.addIntegerArgument(4, false, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null) {
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                                sendShieldFrame(frame1,true);
                            }
                        }

                        @Override
                        public void onFinish(int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_FINISH);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            sendShieldFrame(frame1,true);
                        }

                        @Override
                        public void onStart(int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_START);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            sendShieldFrame(frame1,true);
                        }

                        @Override
                        public void onProgress(int bytesWritten, int totalSize, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_PROGRESS);
                            frame1.addIntegerArgument(2, false, requestID);///0=id
                            frame1.addIntegerArgument(2, false, bytesWritten);
                            frame1.addIntegerArgument(2, false, totalSize);
                            sendShieldFrame(frame1,true);
                        }
                    });
                    InternetManager.getInstance().putRequest(requestID, request);
                    break;
                case REQUEST.SET_URL:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setUrl("");
                    break;
                case REQUEST.ADD_HEADER:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).addHeader(frame.getArgumentAsString(1), frame.getArgumentAsString(2));
                    break;
                case REQUEST.ADD_PARAM:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).addParam(frame.getArgumentAsString(1), frame.getArgumentAsString(2));
                    break;
                case REQUEST.DEL_ALL_HEADERS:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().getRequest(requestID).removeAllHeaders();
                    break;
                case REQUEST.DEL_ALL_PARAMS:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().getRequest(requestID).removeAllParams();
                    break;
                case REQUEST.SET_CONTENT_TYPE:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setContentType(frame.getArgumentAsString(1));
                    break;
                case REQUEST.IGNORE_RESPONSE:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).ignoreResponse();
                    break;
                //////// INTERNET
                case INTERNET.GET_REQUEST:
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
                    InternetManager.EXECUTION_TYPE getExecutionType = null;
                    try {
                        getExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.GET);
                        if (getExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addIntegerArgument(2, false, requestID);
                            frame1.addIntegerArgument(1, false, getExecutionType.value);///0=id
                            sendShieldFrame(frame1,true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addIntegerArgument(2, false, requestID);
                        entityError.addIntegerArgument(1, false, RESPONSE.UNSUPPORTED_ENTITY_ENCODING);
                        sendShieldFrame(entityError,true);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.POST_REQUEST:
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
                    try {
                        InternetManager.EXECUTION_TYPE postExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.POST);
                        if (postExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addIntegerArgument(2, false, requestID);
                            frame1.addIntegerArgument(1, false, postExecutionType.value);///0=id
                            sendShieldFrame(frame1);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addIntegerArgument(2, false, requestID);
                        entityError.addIntegerArgument(1, false, RESPONSE.UNSUPPORTED_ENTITY_ENCODING);
                        sendShieldFrame(entityError);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.PUT_REQUEST:
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
                    try {
                        InternetManager.EXECUTION_TYPE putExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.PUT);
                        if (putExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addIntegerArgument(2, false, requestID);
                            frame1.addIntegerArgument(1, false, putExecutionType.value);///0=id
                            sendShieldFrame(frame1);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addIntegerArgument(2, false, requestID);
                        entityError.addIntegerArgument(1, false, 6);
                        sendShieldFrame(entityError);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.DELETE_REQUEST:
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
                    try {
                        InternetManager.EXECUTION_TYPE deleteExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.DELETE);
                        if (deleteExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addIntegerArgument(2, false, requestID);
                            frame1.addIntegerArgument(1, false, deleteExecutionType.value);///0=id
                            sendShieldFrame(frame1);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addIntegerArgument(2, false, requestID);
                        entityError.addIntegerArgument(1, false, 6);
                        sendShieldFrame(entityError);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.CANCEL_ALL_REQUESTS:
                    InternetManager.getInstance().cancelAllRequests();
                    break;
                case INTERNET.SET_BASIC_AUTH:
                    InternetManager.getInstance().setBasicAuth(new Pair<>(frame.getArgumentAsString(0), frame.getArgumentAsString(1)));
                    break;
                case INTERNET.CLEAR_BASIC_AUTH:
                    InternetManager.getInstance().clearBasicAuth();
                    break;
                case INTERNET.SET_DEFAULT_MAX_RESPONSE_BYTES_COUNT:
                    InternetManager.getInstance().setMaxSentBytes(frame.getArgumentAsInteger(0));
                    break;
                /////// RESPONSE
                case RESPONSE.DISPOSE:
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().disponseResponse(requestID);
                    break;
                case RESPONSE.GET_NEXT_BYTES:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            final InternetResponse.ResponseBodyBytes bodyBytes = response.getBytes(frame.getArgumentAsInteger(4, 1), frame.getArgumentAsInteger(2));
                            if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.NOT_ENOUGH_BYTES) {
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    ShieldFrame frameSentNotEnough = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_NEXT_BYTES);
                                    frameSentNotEnough.addIntegerArgument(2, false, requestID);
                                    frameSentNotEnough.addArgument(bodyBytes.getArray());
                                    frameSentNotEnough.addIntegerArgument(1, false, RESPONSE.NO_ENOUGH_BYTES);
                                    sendShieldFrame(frameSentNotEnough);
                                }
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_GREATER_THAN_LENGTH || bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_LESS_THAN_0) {
                                ShieldFrame frameSentIndexOut = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                frameSentIndexOut.addIntegerArgument(2, false, requestID);
                                frameSentIndexOut.addIntegerArgument(1, false, RESPONSE.INDEX_OUT_OF_BOUNDS);
                                sendShieldFrame(frameSentIndexOut);
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.COUNT_LESS_THAN_0) {
                                ShieldFrame frameSentcountOut = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                frameSentcountOut.addIntegerArgument(2, false, requestID);
                                frameSentcountOut.addIntegerArgument(1, false, RESPONSE.SIZE_OF_REQUEST_CAN_NOT_BE_ZERO);
                                sendShieldFrame(frameSentcountOut);
                            } else {
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    ShieldFrame frameSent = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_NEXT_BYTES);
                                    frameSent.addIntegerArgument(2, false, requestID);
                                    frameSent.addArgument(bodyBytes.getArray());
                                    sendShieldFrame(frameSent);
                                }
                            }
                        } else {//no response
                            ShieldFrame frameSentNotRes = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameSentNotRes.addIntegerArgument(2, false, requestID);
                            frameSentNotRes.addIntegerArgument(1, false, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameSentNotRes);
                        }
                    } else// no request
                    {
                        ShieldFrame frameSentNotReq = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameSentNotReq.addIntegerArgument(2, false, requestID);
                        frameSentNotReq.addIntegerArgument(1, false, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameSentNotReq);
                    }
                    break;
                case RESPONSE.GET_HEADER:
                    requestID = frame.getArgumentAsInteger(0);
                    String key = frame.getArgumentAsString(1);
                    ShieldFrame frame2 = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                    frame2.addIntegerArgument(2, false, requestID);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            if (response.getHeaders() == null || response.getHeaders().get(key) == null) {
                                frame2.addIntegerArgument(1, false, RESPONSE.HEADER_CAN_NOT_BE_FOUND);
                                sendShieldFrame(frame2);
                            } else {
                                ShieldFrame frameSent = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_HEADER);
                                frameSent.addIntegerArgument(2, false, requestID);
                                frameSent.addStringArgument(key);
                                frameSent.addStringArgument(response.getHeaders().get(key));
                                sendShieldFrame(frameSent);
                            }
                        } else {//no response
                            frame2.addIntegerArgument(1, false, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frame2);
                        }
                    } else// no request
                    {
                        frame2.addIntegerArgument(1, false, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frame2);
                    }
                    break;
                case RESPONSE.GET_JSON_RESPONSE:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            final ArrayList<InternetResponse.JsonNode> jsonNodes = response.getNodes(frame);
                            if (jsonNodes.size() > 0) {
                                try {
                                    String result = response.getValueOf(jsonNodes.get(0).getDataType() == InternetResponse.JsonNode.NODE_DATA_TYPE.ARRAY ? new JSONArray(new String(response.getResponseBody())) : new JSONObject(new String(response.getResponseBody())), jsonNodes);
                                    ShieldFrame frameJsonSent = new ShieldFrame(SHIELD_ID, RESPONSE.RESPONSE_JSON);
                                    frameJsonSent.addIntegerArgument(2, false, requestID);
                                    frameJsonSent.addStringArgument(result);
                                    frameJsonSent.addArgument(frame.getArgument(1));
                                    for (int arg = 2; arg < frame.getArguments().size(); arg++) {
                                        frameJsonSent.addArgument(frame.getArgument(arg));
                                    }
                                    sendShieldFrame(frameJsonSent,true);
                                } catch (JSONException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addIntegerArgument(2, false, requestID);
                                    frameJson.addIntegerArgument(1, false, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson,true);
                                }
                            } else {

                            }
                        } else {//no response
                            ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameJson.addIntegerArgument(2, false, requestID);
                            frameJson.addIntegerArgument(1, false, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameJson,true);
                        }
                    } else// no request
                    {
                        ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameJson.addIntegerArgument(2, false, requestID);
                        frameJson.addIntegerArgument(1, false, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameJson,true);
                    }
                    break;
                case RESPONSE.GET_JSON_ARRAY_LENGTH:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            final ArrayList<InternetResponse.JsonNode> jsonNodes = response.getNodes(frame);
                            if (jsonNodes.size() > 0) {
                                try {
                                    int result = response.getJSONArrayLength(jsonNodes.get(0).getDataType() == InternetResponse.JsonNode.NODE_DATA_TYPE.ARRAY ? new JSONArray(new String(response.getResponseBody())) : new JSONObject(new String(response.getResponseBody())), jsonNodes);
                                    if (result == -1) {
                                        ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                        frameJson.addIntegerArgument(2, false, requestID);
                                        frameJson.addIntegerArgument(1, false, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                        sendShieldFrame(frameJson);
                                    } else {
                                        ShieldFrame frameJsonSent = new ShieldFrame(SHIELD_ID, RESPONSE.RESPONSE_JSON_ARRAY);
                                        frameJsonSent.addIntegerArgument(2, false, requestID);
                                        frameJsonSent.addIntegerArgument(4, false, result);
                                        frameJsonSent.addArgument(frame.getArgument(1));
                                        for (int arg = 2; arg < frame.getArguments().size(); arg++) {
                                            frameJsonSent.addArgument(frame.getArgument(arg));
                                        }
                                        sendShieldFrame(frameJsonSent);
                                    }
                                } catch (JSONException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addIntegerArgument(2, false, requestID);
                                    frameJson.addIntegerArgument(1, false, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson);
                                }
                            } else {

                            }
                        } else {//no response
                            ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameJson.addIntegerArgument(2, false, requestID);
                            frameJson.addIntegerArgument(1, false, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameJson);
                        }
                    } else// no request
                    {
                        ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameJson.addIntegerArgument(2, false, requestID);
                        frameJson.addIntegerArgument(1, false, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameJson);
                    }
                    break;
                case REQUEST.ADD_HTTP_ENTITY:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setEntity(frame.getArgumentAsString(1));
                    break;
                case REQUEST.SET_ENCODING:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).setEncoding(frame.getArgumentAsString(1));
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
            if (mainReq.isCancelled())
                req.setCancelled();
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
