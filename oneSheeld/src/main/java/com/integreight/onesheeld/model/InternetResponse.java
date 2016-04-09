package com.integreight.onesheeld.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.integreight.onesheeld.sdk.ShieldFrame;
import com.integreight.onesheeld.shields.controller.utils.InternetManager;
import com.integreight.onesheeld.utils.BitsUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Saad on 1/26/15.
 */
public class InternetResponse implements Parcelable, Serializable {
    private byte[] responseBody;
    private int currentBound = 0;
    private int statusCode = -1;
    private RESPONSE_STATUS status;
    private Hashtable<String, String> headers;
    private Object jsonResponse;
    private RESPONSE_TYPE responseType;


    public InternetResponse() {
        status = RESPONSE_STATUS.IN_QUEUE;
        currentBound = 0;
        statusCode = -1;
        responseBody = null;
        headers = null;
    }

    public InternetResponse(byte[] responseBody, int statusCode, RESPONSE_STATUS status, cz.msebera.android.httpclient.Header[] headers) {
        this();
        this.responseBody = responseBody != null ? responseBody : new byte[0];
        this.statusCode = statusCode;
        this.status = status;
        this.setHeaders(headers);
    }

    public byte[] next() {
        return next(InternetManager.getInstance().getMaxSentBytes());
    }

    public byte[] next(int targetBound) {
        if (responseBody == null || status == RESPONSE_STATUS.DONE)
            return null;
        targetBound = responseBody.length - currentBound >= targetBound ? targetBound : responseBody.length - currentBound;
        byte[] response = new byte[targetBound];
        for (int i = 0; i < response.length; i++)
            response[i] = responseBody[currentBound + i];
        currentBound = currentBound + targetBound;
        if (currentBound >= responseBody.length)
            status = RESPONSE_STATUS.DONE;
        return response;
    }

    public ResponseBodyBytes getBytes(int index, int count) {
        ResponseBodyBytes res = new ResponseBodyBytes();
        if (index > responseBody.length)
            return new ResponseBodyBytes(null, RESPONSE_BODY_BYTES.INDEX_GREATER_THAN_LENGTH);
        if (index < 0)
            return new ResponseBodyBytes(null, RESPONSE_BODY_BYTES.INDEX_LESS_THAN_0);
        if (count <= 0)
            return new ResponseBodyBytes(new byte[0], RESPONSE_BODY_BYTES.COUNT_LESS_THAN_0);
        count = count > 255 ? 255 : count;
        res.setBytes_status(RESPONSE_BODY_BYTES.SUCCESS);
        int targetBound = index + count;
        if (responseBody.length < targetBound)
            res.setBytes_status(RESPONSE_BODY_BYTES.NOT_ENOUGH_BYTES);
        targetBound = responseBody.length >= targetBound ? count : responseBody.length - index;
        byte[] response = new byte[targetBound];
        for (int i = 0; i < response.length; i++)
            response[i] = responseBody[index + i];
        res.setArray(response);
        return res;
    }

    public ArrayList<JsonNode> getNodes(ShieldFrame frame) {
        ArrayList<JsonNode> nodes = new ArrayList<>();
        int dataType = frame.getArgumentAsInteger(1);
        for (int i = 2; i < frame.getArguments().size(); i++) {
            JsonNode node = new JsonNode();
            if (BitsUtils.isBitSet(dataType, i - 2)) {
                node.setDataType(JsonNode.NODE_DATA_TYPE.OBJECT);
                node.setKey(frame.getArgumentAsString(i));
            } else {
                node.setDataType(JsonNode.NODE_DATA_TYPE.ARRAY);
                node.setIndex(frame.getArgumentAsInteger(i));
            }
            nodes.add(node);
        }
        return nodes;
    }

    public String getValueOf(Object object, ArrayList<JsonNode> tree) throws JSONException, ClassCastException {
        final JsonNode node = tree.get(0);
        if (node != null && object != null) {
            if (node.getDataType() == JsonNode.NODE_DATA_TYPE.OBJECT) {
                JSONObject jsonObject = (JSONObject) object;
                if (tree.size() == 1)
                    return jsonObject.getString(node.getKey());
                else {
                    tree.remove(0);
                    return getValueOf(jsonObject.get(node.getKey()), tree);
                }
            } else if (node.getDataType() == JsonNode.NODE_DATA_TYPE.ARRAY) {
                JSONArray jsonArray = (JSONArray) object;
                if (tree.size() == 1)
                    return jsonArray.getString(node.getIndex());
                else {
                    tree.remove(0);
                    return getValueOf(jsonArray.get(node.getIndex()), tree);
                }
            }
        }
        return null;
    }

    public int getJSONArrayLength(Object object, ArrayList<JsonNode> tree) throws JSONException, ClassCastException {
        final JsonNode node = tree.get(0);
        if (node.getDataType() == JsonNode.NODE_DATA_TYPE.OBJECT) {
            JSONObject jsonObject = (JSONObject) object;
            if (tree.size() == 1)
                return jsonObject.getJSONArray(node.getKey()).length();
            else {
                tree.remove(0);
                return getJSONArrayLength(jsonObject.get(node.getKey()), tree);
            }
        } else if (node.getDataType() == JsonNode.NODE_DATA_TYPE.ARRAY)

        {
            JSONArray jsonArray = (JSONArray) object;
            if (tree.size() == 1)
                return jsonArray.length();
            else {
                tree.remove(0);
                return getJSONArrayLength(jsonArray.get(node.getIndex()), tree);
            }
        }

        return -1;
    }

    public byte[] getResponseBody() {
        return responseBody == null ? new byte[]{} : responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody != null ? responseBody : new byte[0];
    }

    public int getCurrentBound() {
        return currentBound;
    }

    public void setCurrentBound(int currentBound) {
        this.currentBound = currentBound;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public RESPONSE_STATUS getStatus() {
        return status;
    }

    public void setStatus(RESPONSE_STATUS status) {
        this.status = status;
    }

    public Hashtable<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(cz.msebera.android.httpclient.Header[] headers1) {
        this.headers = new Hashtable<>();
        if (headers1 != null) {
            for (cz.msebera.android.httpclient.Header header : headers1) {
                headers.put(header.getName(), header.getValue());
            }
        }
    }

    public RESPONSE_TYPE getResponseType() {
        return responseType;
    }

    public void setResponseType(RESPONSE_TYPE responseType) {
        this.responseType = responseType;
    }

    protected InternetResponse(Parcel in) {
        currentBound = in.readInt();
        statusCode = in.readInt();
        status = (RESPONSE_STATUS) in.readValue(RESPONSE_STATUS.class.getClassLoader());
        headers = (Hashtable) in.readValue(Hashtable.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(currentBound);
        dest.writeInt(statusCode);
        dest.writeValue(status);
        dest.writeValue(headers);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<InternetResponse> CREATOR = new Parcelable.Creator<InternetResponse>() {
        @Override
        public InternetResponse createFromParcel(Parcel in) {
            return new InternetResponse(in);
        }

        @Override
        public InternetResponse[] newArray(int size) {
            return new InternetResponse[size];
        }
    };

    public enum RESPONSE_STATUS implements Serializable {
        SUCCESSFUL, FAILURE, IN_QUEUE, DONE;
    }

    public class ResponseBodyBytes {
        private byte[] array;
        RESPONSE_BODY_BYTES bytes_status;

        public ResponseBodyBytes() {

        }

        public ResponseBodyBytes(byte[] array, RESPONSE_BODY_BYTES bytes_status) {
            this.array = array;
            this.bytes_status = bytes_status;
        }

        public byte[] getArray() {
            return array;
        }

        public void setArray(byte[] array) {
            this.array = array;
        }

        public RESPONSE_BODY_BYTES getBytes_status() {
            return bytes_status;
        }

        public void setBytes_status(RESPONSE_BODY_BYTES bytes_status) {
            this.bytes_status = bytes_status;
        }
    }

    public enum RESPONSE_BODY_BYTES {
        SUCCESS(-1), NOT_ENOUGH_BYTES(3), INDEX_LESS_THAN_0(0), INDEX_GREATER_THAN_LENGTH(0), COUNT_LESS_THAN_0(4);
        public int value = -1;

        private RESPONSE_BODY_BYTES(int value) {
            this.value = value;
        }
    }

    public enum RESPONSE_TYPE {
        HTML, JSON
    }

    public static class JsonNode {
        private NODE_DATA_TYPE nodeDataType;
        private String key;
        private int index = -1;

        public JsonNode(NODE_DATA_TYPE nodeDataType, String key) {
            this.nodeDataType = nodeDataType;
            this.key = key;
        }

        public JsonNode(NODE_DATA_TYPE nodeDataType, int index) {
            this.nodeDataType = nodeDataType;
            this.index = index;
        }

        public JsonNode() {
        }

        public NODE_DATA_TYPE getDataType() {
            return nodeDataType;
        }

        public void setDataType(NODE_DATA_TYPE nodeDataType) {
            this.nodeDataType = nodeDataType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public static enum NODE_DATA_TYPE {
            OBJECT, ARRAY
        }
    }
}