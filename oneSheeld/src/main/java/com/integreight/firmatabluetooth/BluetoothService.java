package com.integreight.firmatabluetooth;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.integreight.onesheeld.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BluetoothService {
    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean D = true;
    private BluetoothDevice mmDevice;

    public static interface BluetoothServiceHandler {
        void onDataReceived(byte[] bytes, int length);

        void onStateChanged(int state, boolean isManually);

        void onDataWritten(byte[] bytes);

        void onConnected(BluetoothDevice device);

        void onError(String error);

    }

    private CopyOnWriteArrayList<BluetoothServiceHandler> handlers;


    // Unique UUID for this application
    public static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    // private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
    // device

    private boolean closedManually = false;

    public BluetoothService(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        this.handlers = new CopyOnWriteArrayList<BluetoothServiceHandler>();

    }

    public void addBluetoothServiceHandler(BluetoothServiceHandler handler) {
        if (!handlers.contains(handler))
            handlers.add(handler);
    }

    public synchronized void closeSocket(BluetoothSocket socket)
            throws IOException {
        if (socket != null)
            socket.close();
    }

    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        // mHandler.obtainMessage(BluetoothService.MESSAGE_STATE_CHANGE, state,
        // closedManually?1:0).sendToTarget();
        for (BluetoothServiceHandler handler : handlers) {
            handler.onStateChanged(state, closedManually);
        }
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void connect(BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connect to: " + device);
        closedManually = false;
        // Cancel any thread attempting to make a connection
        // if (mState == STATE_CONNECTING) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }
        // }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
    }

    private synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread.interrupt();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        mmDevice = null;
        setState(STATE_NONE);
    }

    public synchronized void stopConnection() {
        closedManually = true;
        stop();
    }

    private synchronized BluetoothSocket getRfcommSocketByReflection()
            throws Exception {
        if (mmDevice == null)
            return null;
        Method m = mmDevice.getClass().getMethod("createRfcommSocket",
                new Class[]{int.class});

        return (BluetoothSocket) m.invoke(mmDevice, 1);
    }

    private synchronized BluetoothSocket getInsecureRfcommSocketByReflection()
            throws Exception {
        if (mmDevice == null)
            return null;
        Method m = mmDevice.getClass().getMethod("createInsecureRfcommSocket",
                new Class[]{int.class});

        return (BluetoothSocket) m.invoke(mmDevice, 1);
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (getState() != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    public void write(byte writeData) {
        byte[] _writeData = {(byte) writeData};
        write(_writeData);
    }

    private void connectionFailed() {
        for (BluetoothServiceHandler handler : handlers) {
            handler.onError("Unable to connect device");
        }
        stop();
    }

    private void connectionLost() {
        stop();
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket = null;

        // private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (Build.VERSION.SDK_INT < 10) {
                    mmSocket = getInsecureRfcommSocketByReflection();
                } else {
                    mmSocket = device
                            .createInsecureRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (Exception e) {
                Log.e("TAG", "Blutooth Service ConnectThread()", e);
                try {
                    mmSocket = device
                            .createRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                    // Log.e(TAG, "create() failed", e);
                    try {
                        mmSocket = getRfcommSocketByReflection();
                    } catch (Exception e2) {
                        // TODO Auto-generated catch block
                        e2.printStackTrace();
                        return;
                    }

                }
            }
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            while (mAdapter.isDiscovering())
                mAdapter.cancelDiscovery();
            if (mAdapter != null)
                mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                if (Thread.currentThread().isInterrupted()) {
                    cancel();
                    // connectionFailed();
                    return;
                }
                mmSocket.connect();
                if (Thread.currentThread().isInterrupted()) {
                    cancel();
                    // connectionFailed();
                    return;
                }
            } catch (IOException e) {

                try {
                    if (Thread.currentThread().isInterrupted()) {
                        cancel();
                        // connectionFailed();
                        return;
                    }
                    if (Build.VERSION.SDK_INT >= 10)
                        mmSocket = getInsecureRfcommSocketByReflection();
                    else
                        mmSocket = mmDevice
                                .createRfcommSocketToServiceRecord(MY_UUID);
                    if (Thread.currentThread().isInterrupted()) {
                        cancel();
                        // connectionFailed();
                        return;
                    }
                    mmSocket.connect();
                    if (Thread.currentThread().isInterrupted()) {
                        cancel();
                        // connectionFailed();
                        return;
                    }
                } catch (Exception e1) {

                    try {
                        if (Thread.currentThread().isInterrupted()) {
                            cancel();
                            // connectionFailed();
                            return;
                        }
                        if (Build.VERSION.SDK_INT >= 10)
                            mmSocket = mmDevice
                                    .createRfcommSocketToServiceRecord(MY_UUID);
                        else
                            mmSocket = getRfcommSocketByReflection();
                        if (Thread.currentThread().isInterrupted()) {
                            cancel();
                            // connectionFailed();
                            return;
                        }
                        mmSocket.connect();
                        if (Thread.currentThread().isInterrupted()) {
                            cancel();
                            // connectionFailed();
                            return;
                        }
                    } catch (Exception e2) {
                        if (Build.VERSION.SDK_INT >= 10) {
                            try {
                                if (Thread.currentThread().isInterrupted()) {
                                    cancel();
                                    // connectionFailed();
                                    return;
                                }
                                mmSocket = getRfcommSocketByReflection();
                                if (Thread.currentThread().isInterrupted()) {
                                    cancel();
                                    // connectionFailed();
                                    return;
                                }
                                mmSocket.connect();
                                if (Thread.currentThread().isInterrupted()) {
                                    cancel();
                                    // connectionFailed();
                                    return;
                                }
                            } catch (Exception e5) {
                                connectionFailed();
                                return;
                            }
                        } else {
                            connectionFailed();
                            return;
                        }
                    }
                }

            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
            if (Thread.currentThread().isInterrupted()) {
                cancel();
                connectionFailed();
                return;
            }
        }

        public synchronized void cancel() {
            try {
                closeSocket(mmSocket);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        Handler writeHandler;
        Looper writeHandlerLooper;
        Thread LooperThread;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
                // e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            if (mmDevice == null)
                return;
            LooperThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Looper.prepare();
                    writeHandlerLooper = Looper.myLooper();
                    writeHandler = new Handler();
                    Looper.loop();
                }
            });
            LooperThread.start();
            while (!LooperThread.isAlive())
                ;
            setState(STATE_CONNECTED);
            for (BluetoothServiceHandler handler : handlers) {
                handler.onConnected(mmDevice);
            }
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer, 0, buffer.length);
                    for (BluetoothServiceHandler handler : handlers) {
                        handler.onDataReceived(buffer, bytes);
                    }
                } catch (IOException e) {
                    // e.printStackTrace();
                    Log.e(TAG, "disconnected", e);
                    // if(!closedManually)
                    if (writeHandlerLooper != null)
                        writeHandlerLooper.quit();
                    connectionLost();
                    break;
                }
            }
        }

        public synchronized void write(final byte[] buffer) {
            if (writeHandler == null)
                return;
            writeHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        mmOutStream.write(buffer);
                        for (BluetoothServiceHandler handler : handlers) {
                            handler.onDataWritten(buffer);
                        }

                    } catch (IOException e) {
                        if (writeHandlerLooper != null)
                            writeHandlerLooper.quit();
                        connectionLost();
                        Log.e(TAG, "Exception during write", e);
                    }
                }
            });

        }

        public synchronized void cancel() {
            try {
                closeSocket(mmSocket);
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
