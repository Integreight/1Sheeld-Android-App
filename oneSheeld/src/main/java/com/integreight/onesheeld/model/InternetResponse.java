package com.integreight.onesheeld.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.integreight.onesheeld.shields.controller.utils.InternetManager;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
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

    public InternetResponse(byte[] responseBody, int statusCode, RESPONSE_STATUS status, Header[] headers) {
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

    public String getValueOf(JSONObject json, String... tree) throws JSONException {
        String value = null;
        if (tree.length == 1)
            return json.getString(tree[0]);
        int i = 0;
        for (String item : tree) {
            if (item != null) {
                Object obj = json.get(item);
                if (obj != null) {
                    if (obj instanceof JSONObject) {
                        tree[i] = null;
                        try {
                            return getValueOf(new JSONObject(obj.toString()), tree);
                        } catch (JSONException e) {
                            if (i == tree.length - 1)
                                return obj.toString();
                        }
                    } else if (obj instanceof JSONArray) {

                    }
                }
            }
            i++;
        }
//        Iterator<String> iter = json.keys();
//        while (iter.hasNext()) {
//            String key = iter.next();
//        }
        return value;
    }

    public byte[] getResponseBody() {
        return responseBody;
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

    public void setHeaders(Header[] headers1) {
        this.headers = new Hashtable<>();
        if (headers1 != null) {
            for (Header header : headers1) {
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
}