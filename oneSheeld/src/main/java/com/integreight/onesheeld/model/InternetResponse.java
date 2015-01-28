package com.integreight.onesheeld.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.Header;

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

    public InternetResponse() {
        status = RESPONSE_STATUS.IN_QUEUE;
        currentBound = 0;
        statusCode = -1;
        responseBody = null;
        headers = null;
    }

    public InternetResponse(byte[] responseBody, int statusCode, RESPONSE_STATUS status, Header[] headers) {
        this();
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.status = status;
        this.setHeaders(headers);
    }

    public byte[] next() {
        return next(255);
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

    public byte[] getBytes(int index, int count) {
        if (index > responseBody.length)
            return null;
        int targetBound = index + count;
        targetBound = responseBody.length - index >= targetBound ? targetBound : responseBody.length - index;
        byte[] response = new byte[targetBound];
        for (int i = 0; i < response.length; i++)
            response[i] = responseBody[index + i];
        return response;
    }

    public byte[] getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(byte[] responseBody) {
        this.responseBody = responseBody;
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
}