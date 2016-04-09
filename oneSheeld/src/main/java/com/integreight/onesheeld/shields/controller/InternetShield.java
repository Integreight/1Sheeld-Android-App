package com.integreight.onesheeld.shields.controller;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.util.Pair;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.model.InternetRequest;
import com.integreight.onesheeld.model.InternetResponse;
import com.integreight.onesheeld.model.InternetUiRequest;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.ControllerParent;
import com.integreight.onesheeld.shields.controller.utils.CameraUtils;
import com.integreight.onesheeld.shields.controller.utils.InternetManager;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.snappydb.SnappydbException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;

/*
* author Saad
*
* Internet shield depends on a major single to class called InternetManager which contains most of the logic,
* receiving and sending frames only here
* */
public class InternetShield extends
        ControllerParent<InternetShield> {
    private static final byte SHIELD_ID = UIShield.INTERNET_SHIELD.getId();

    /*
        * a static final class that caontains sent/received frames related to request frames
    * */
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
        public static final byte ADD_LAST_IMAGE_AS_PARAM = (byte) 0x18;
        public static final byte HTTP_ADD_LAST_IMAGE_AS_RAW_ENTITY = (byte) 0x19;

        ////// SENT FRAMES FUNICTIONS IDs
        private static final byte ON_SUCCESS = (byte) 0x01;
        private static final byte ON_FAILURE = (byte) 0x02;
        private static final byte ON_START = (byte) 0x03;
        private static final byte ON_PROGRESS = (byte) 0x04;
        private static final byte ON_FINISH = (byte) 0x05;
    }

    private final static class IMAGE_ENCODING {
        private static final byte MULTIPART = (byte) 0x00;
        private static final byte BASE64 = (byte) 0x01;
    }

    /*
        * a static final class that caontains sent/received frames related to internet funs. frames
    * */
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
        public static final int URL_IS_WRONG = 4;
    }

    /*
        * a static final class that caontains sent/received frames related to response frames
    * */
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
        public static final byte ON_NOT_ENOUGH_BYTES = (byte) 0x06;

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

    @Override
    public ControllerParent<InternetShield> init(String tag) {
        // TODO Auto-generated method stub\
        try {
            InternetManager.getInstance();
            // initializing local db for fetched responses
            if (InternetManager.getInstance().getCachDB() == null || !InternetManager.getInstance().getCachDB().isOpen())
                InternetManager.getInstance().init(getApplication());
            else {
                if (!InternetManager.getInstance().getCachDB().isOpen()) {
                    InternetManager.getInstance().close();
                    InternetManager.resetInstance().init(getApplication());
                }
            }
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
//        if (true)
//            throw new ClassCastException();
        return super.init(tag);
    }

    public InternetShield(Activity activity, String tag) {
        super(activity, tag);
    }

    public InternetShield() {
        super();
    }

    public interface CallBack {
        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, int RequestID);

        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error, int RequestID);

        public void onFinish(int requestID);

        public void onStart(int requestID);

    }

    int requestID = 0;

    @Override
    public void onNewShieldFrameReceived(ShieldFrame frame) {
        if (frame.getShieldId() == UIShield.INTERNET_SHIELD.getId()) {
            switch (frame.getFunctionId()) {
                case REQUEST.NEW_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    final InternetRequest request = new InternetRequest();
                    request.setId(requestID);
                    String url = frame.getArgumentAsString(1);
                    request.setUrl(url);
                    request.setCallback(new CallBack() {
                        @Override
                        public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_SUCCESS);
                            frame1.addArgument(2, requestID);///0=id
                            frame1.addArgument(2, statusCode);//
                            frame1.addArgument(4, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null) {
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                                sendShieldFrame(frame1, true);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error, int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_FAILURE);
                            frame1.addArgument(2, requestID);
                            frame1.addArgument(2, statusCode);//
                            frame1.addArgument(4, responseBody != null ? responseBody.length : 0);
                            InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                            if (response != null) {
                                frame1.addArgument(response.getBytes(0, InternetManager.getInstance().getMaxSentBytes()).getArray());
                                sendShieldFrame(frame1, true);
                            }
                        }

                        @Override
                        public void onFinish(final int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_FINISH);
                            frame1.addArgument(2, requestID);
                            if (request.getRegisteredCallbacks().contains(InternetRequest.CALLBACK.ON_SUCCESS.name()) || request.getRegisteredCallbacks().contains(InternetRequest.CALLBACK.ON_FAILURE.name()))
                                queueShieldFrame(frame1);
                            else
                                sendShieldFrame(frame1, true);
                        }

                        @Override
                        public void onStart(int requestID) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, REQUEST.ON_START);
                            frame1.addArgument(2, requestID);
                            sendShieldFrame(frame1, true);
                        }
                    });
                    InternetManager.getInstance().putRequest(requestID, request);
                    break;
                case REQUEST.SET_URL:
                    requestID = frame.getArgumentAsInteger(0);
                    String reqURL = frame.getArgumentAsString(1);
                    if (!reqURL.contains(" ")) {
                        if (InternetManager.getInstance().getRequest(requestID) != null)
                            InternetManager.getInstance().getRequest(requestID).setUrl(reqURL);
                    } else {
                        ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        frame1.addArgument(2, requestID);
                        frame1.addArgument(1, INTERNET.URL_IS_WRONG);
                        sendShieldFrame(frame1, true);
                    }
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
                case REQUEST.ADD_LAST_IMAGE_AS_PARAM:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        String imgPath = null;
                        byte sourceFolderId = frame.getArgument(2)[0];
                        if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                            imgPath = CameraUtils.getLastCapturedImagePathFromOneSheeldFolder(activity,true);
                        else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                            imgPath = CameraUtils.getLastCapturedImagePathFromCameraFolder(activity);
                        if (imgPath != null) {
                            if (frame.getArgument(3)[0] == IMAGE_ENCODING.MULTIPART)
                                InternetManager.getInstance().getRequest(requestID).addFile(frame.getArgumentAsString(1), imgPath);
                            else if (frame.getArgument(3)[0] == IMAGE_ENCODING.BASE64)
                                InternetManager.getInstance().getRequest(requestID).addBase64File(frame.getArgumentAsString(1), new File(imgPath));
                        }
                    }
                    break;
                case REQUEST.HTTP_ADD_LAST_IMAGE_AS_RAW_ENTITY:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        String imgPath = null;
                        byte sourceFolderId = frame.getArgument(1)[0];
                        if (sourceFolderId == CameraUtils.FROM_ONESHEELD_FOLDER)
                            imgPath = CameraUtils.getLastCapturedImagePathFromOneSheeldFolder(activity,true);
                        else if (sourceFolderId == CameraUtils.FROM_CAMERA_FOLDER)
                            imgPath = CameraUtils.getLastCapturedImagePathFromCameraFolder(activity);
                        if (imgPath != null) {
//                                InternetManager.getInstance().getRequest(requestID).setContentType("image/jpeg");
//                                InternetManager.getInstance().getRequest(requestID).addHeader("Content-Length", String.valueOf(new File(imgPath).length()));
                            InternetManager.getInstance().getRequest(requestID).setEntity(null);
                            InternetManager.getInstance().getRequest(requestID).setFileEntity(imgPath);
                        }
                    }
                    break;
                case REQUEST.DEL_ALL_HEADERS:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
                        InternetManager.getInstance().getRequest(requestID).removeAllHeaders();
                    break;
                case REQUEST.DEL_ALL_PARAMS:
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null)
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
                    InternetManager.EXECUTION_TYPE getExecutionType = null;
                    try {
                        getExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.GET, frame.getArgument(1)[0]);
                        //request excutes only in case of success
                        if (getExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addArgument(2, requestID);
                            frame1.addArgument(1, getExecutionType.value);
                            sendShieldFrame(frame1, true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        //dummy catch that only used for requests the accept custom entity
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addArgument(2, requestID);
                        entityError.addArgument(1, RESPONSE.UNSUPPORTED_ENTITY_ENCODING);
                        sendShieldFrame(entityError, true);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.POST_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    try {
                        InternetManager.EXECUTION_TYPE postExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.POST, frame.getArgument(1)[0]);
                        if (postExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addArgument(2, requestID);
                            frame1.addArgument(1, postExecutionType.value);///0=id
                            sendShieldFrame(frame1, true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addArgument(2, requestID);
                        entityError.addArgument(1, RESPONSE.UNSUPPORTED_ENTITY_ENCODING);
                        sendShieldFrame(entityError, true);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.PUT_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    try {
                        InternetManager.EXECUTION_TYPE putExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.PUT, frame.getArgument(1)[0]);
                        if (putExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addArgument(2, requestID);
                            frame1.addArgument(1, putExecutionType.value);///0=id
                            sendShieldFrame(frame1, true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addArgument(2, requestID);
                        entityError.addArgument(1, 6);
                        sendShieldFrame(entityError, true);
                        e.printStackTrace();
                    }
                    break;
                case INTERNET.DELETE_REQUEST:
                    requestID = frame.getArgumentAsInteger(0);
                    try {
                        InternetManager.EXECUTION_TYPE deleteExecutionType = InternetManager.getInstance().execute(requestID, InternetManager.REQUEST_TYPE.DELETE, frame.getArgument(1)[0]);
                        if (deleteExecutionType != InternetManager.EXECUTION_TYPE.SUCCESSFUL) {
                            ShieldFrame frame1 = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                            frame1.addArgument(2, requestID);
                            frame1.addArgument(1, deleteExecutionType.value);///0=id
                            sendShieldFrame(frame1, true);
                        }
                    } catch (UnsupportedEncodingException e) {
                        ShieldFrame entityError = new ShieldFrame(SHIELD_ID, INTERNET.ON_ERROR);
                        entityError.addArgument(2, requestID);
                        entityError.addArgument(1, 6);
                        sendShieldFrame(entityError, true);
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
                case RESPONSE.DISPOSE:// remove response from database
                    requestID = frame.getArgumentAsInteger(0);
                    InternetManager.getInstance().disponseResponse(requestID);
                    break;
                case RESPONSE.GET_NEXT_BYTES: // get a byte array for the response from index to another
                    requestID = frame.getArgumentAsInteger(0);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            final InternetResponse.ResponseBodyBytes bodyBytes = response.getBytes(frame.getArgumentAsInteger(1), frame.getArgumentAsInteger(2));
                            if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.NOT_ENOUGH_BYTES) {
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    ShieldFrame frameSent = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_NEXT_BYTES);
                                    frameSent.addArgument(2, requestID);
                                    frameSent.addArgument(bodyBytes.getArray());
                                    sendShieldFrame(frameSent, true);
                                    ShieldFrame frameSentNotEnough = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameSentNotEnough.addArgument(2, requestID);
                                    frameSentNotEnough.addArgument(1, RESPONSE.NO_ENOUGH_BYTES);
                                    queueShieldFrame(frameSentNotEnough);
                                }
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_GREATER_THAN_LENGTH || bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.INDEX_LESS_THAN_0) {
                                ShieldFrame frameSentIndexOut = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                frameSentIndexOut.addArgument(2, requestID);
                                frameSentIndexOut.addArgument(1, RESPONSE.INDEX_OUT_OF_BOUNDS);
                                sendShieldFrame(frameSentIndexOut, true);
                            } else if (bodyBytes.getBytes_status() == InternetResponse.RESPONSE_BODY_BYTES.COUNT_LESS_THAN_0) {
                                ShieldFrame frameSentcountOut = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                frameSentcountOut.addArgument(2, requestID);
                                frameSentcountOut.addArgument(1, RESPONSE.SIZE_OF_REQUEST_CAN_NOT_BE_ZERO);
                                sendShieldFrame(frameSentcountOut, true);
                            } else { // successfully getten fully
                                if (bodyBytes.getArray() != null && bodyBytes.getArray().length > 0) {
                                    ShieldFrame frameSent = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_NEXT_BYTES);
                                    frameSent.addArgument(2, requestID);
                                    frameSent.addArgument(bodyBytes.getArray());
                                    sendShieldFrame(frameSent, true);
                                }
                            }
                        } else {//no response
                            ShieldFrame frameSentNotRes = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameSentNotRes.addArgument(2, requestID);
                            frameSentNotRes.addArgument(1, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameSentNotRes, true);
                        }
                    } else// no request
                    {
                        ShieldFrame frameSentNotReq = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameSentNotReq.addArgument(2, requestID);
                        frameSentNotReq.addArgument(1, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameSentNotReq, true);
                    }
                    break;
                case RESPONSE.GET_HEADER:
                    requestID = frame.getArgumentAsInteger(0);
                    String key = frame.getArgumentAsString(1);
                    ShieldFrame frame2 = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                    frame2.addArgument(2, requestID);
                    if (InternetManager.getInstance().getRequest(requestID) != null) {
                        InternetResponse response = InternetManager.getInstance().getRequest(requestID).getResponse();
                        if (response != null) {
                            if (response.getHeaders() == null || response.getHeaders().get(key) == null) {
                                frame2.addArgument(1, RESPONSE.HEADER_CAN_NOT_BE_FOUND);
                                sendShieldFrame(frame2, true);
                            } else {
                                ShieldFrame frameSent = new ShieldFrame(SHIELD_ID, RESPONSE.SEND_GET_HEADER);
                                frameSent.addArgument(2, requestID);
                                frameSent.addArgument(key);
                                frameSent.addArgument(response.getHeaders().get(key));
                                sendShieldFrame(frameSent, true);
                            }
                        } else {//no response
                            frame2.addArgument(1, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frame2, true);
                        }
                    } else// no request
                    {
                        frame2.addArgument(1, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frame2, true);
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
                                    frameJsonSent.addArgument(2, requestID);
                                    frameJsonSent.addArgument(result);
                                    frameJsonSent.addArgument(frame.getArgument(1));
                                    for (int arg = 2; arg < frame.getArguments().size(); arg++) {
                                        frameJsonSent.addArgument(frame.getArgument(arg));
                                    }
                                    sendShieldFrame(frameJsonSent, true);
                                } catch (JSONException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addArgument(2, requestID);
                                    frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson, true);
                                } catch (ClassCastException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addArgument(2, requestID);
                                    frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson, true);
                                }
                            } else {
                                ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                frameJson.addArgument(2, requestID);
                                frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                sendShieldFrame(frameJson, true);
                            }
                        } else {//no response
                            ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameJson.addArgument(2, requestID);
                            frameJson.addArgument(1, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameJson, true);
                        }
                    } else// no request
                    {
                        ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameJson.addArgument(2, requestID);
                        frameJson.addArgument(1, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameJson, true);
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
                                        frameJson.addArgument(2, requestID);
                                        frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                        sendShieldFrame(frameJson, true);
                                    } else {
                                        ShieldFrame frameJsonSent = new ShieldFrame(SHIELD_ID, RESPONSE.RESPONSE_JSON_ARRAY);
                                        frameJsonSent.addArgument(2, requestID);
                                        frameJsonSent.addArgument(4, result);
                                        frameJsonSent.addArgument(frame.getArgument(1));
                                        for (int arg = 2; arg < frame.getArguments().size(); arg++) {
                                            frameJsonSent.addArgument(frame.getArgument(arg));
                                        }
                                        sendShieldFrame(frameJsonSent, true);
                                    }
                                } catch (JSONException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addArgument(2, requestID);
                                    frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson, true);
                                } catch (ClassCastException e) {
                                    ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                                    frameJson.addArgument(2, requestID);
                                    frameJson.addArgument(1, RESPONSE.JSON_KEYCHAIN_IS_WRONG);
                                    sendShieldFrame(frameJson, true);
                                }
                            } else {

                            }
                        } else {//no response
                            ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                            frameJson.addArgument(2, requestID);
                            frameJson.addArgument(1, RESPONSE.REQUEST_HAS_NO_RESPONSE);
                            sendShieldFrame(frameJson, true);
                        }
                    } else// no request
                    {
                        ShieldFrame frameJson = new ShieldFrame(SHIELD_ID, RESPONSE.ON_ERROR);
                        frameJson.addArgument(2, requestID);
                        frameJson.addArgument(1, RESPONSE.RESPONSE_CAN_NOT_BE_FOUND);
                        sendShieldFrame(frameJson, true);
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

    /*
    * get a list of request to show in the fragment
    * */
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
            req.setFiles(mainReq.getFilesAsMap());
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
            if (req.getParams() != null) {
                params = "";
                if (req.getParamsAsMap().size() > 0) {
                    for (String key : req.getParamsAsMap().keySet()) {
                        if (req.getParamsAsMap().get(key).length() > 100)
                            params += key + " : " + req.getParamsAsMap().get(key).substring(0,100) + "\n";
                        else
                            params += key + " : " + req.getParamsAsMap().get(key) + "\n";
                    }
                }
                if (req.getFilesAsMap().size() > 0) {
                    for (String key : req.getFilesAsMap().keySet()) {
                        params += key + " : " + req.getFilesAsMap().get(key) + "\n";
                    }
                }
            }
            children.add(new Pair<>("Parameters", params));
            String headers = "No Headers";
            if (req.getHeaders() != null) {
                headers = "";
                for (cz.msebera.android.httpclient.Header header : req.getHeaders()) {
                    headers += header.getName() + " : " + header.getValue() + "\n";
                }
            }
            children.add(new Pair<>("Headers", headers));
            req.setUiChildren(children);
            requestsUI.add(req);
        }
        return requestsUI;
    }

    @Override
    public ControllerParent<InternetShield> invalidate(SelectionAction selectionAction, boolean isToastable) {
        this.selectionAction =selectionAction;
        if(Build.VERSION.SDK_INT >=16)
        addRequiredPremission(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (checkForPermissions()) {
            if (selectionAction != null)
                selectionAction.onSuccess();
        }else {
            if (selectionAction != null)
                selectionAction.onFailure();
        }
        return super.invalidate(selectionAction, isToastable);
    }


}
