package com.integreight.firmatabluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

import com.integreight.firmatabluetooth.BluetoothService.BluetoothServiceHandler;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ArrayUtils;
import com.integreight.onesheeld.utils.Log;
import com.integreight.onesheeld.utils.TimeOut;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ArduinoFirmata {
    public final static String TAG = "ArduinoFirmata";

    LinkedBlockingQueue<Byte> uartBuffer;
    LinkedBlockingQueue<Byte> bluetoothBuffer;
    UartListeningThread uartListeningThread;
    BluetoothBufferListeningThread bluetoothBufferListeningThread;
    TimeOut ShieldFrameTimeout;
    boolean isBootloader = false;

    public static final byte INPUT = 0;
    public static final byte OUTPUT = 1;
    public static final byte PWM = 3;
    public static final byte SERVO = 4;
    public static final boolean LOW = false;
    public static final boolean HIGH = true;

    private int arduinoLibraryVersion = -1;

    private final char MAX_DATA_BYTES = 4096;
    private final char MAX_OUTPUT_BYTES = 32;
    private final byte DIGITAL_MESSAGE = (byte) 0x90;
    private final byte ANALOG_MESSAGE = (byte) 0xE0;
    private final byte REPORT_ANALOG = (byte) 0xC0;
    private final byte REPORT_DIGITAL = (byte) 0xD0;
    private final byte SET_PIN_MODE = (byte) 0xF4;
    private final byte REPORT_VERSION = (byte) 0xF9;
    private final byte SYSTEM_RESET = (byte) 0xFF;
    private final byte START_SYSEX = (byte) 0xF0;
    private final byte END_SYSEX = (byte) 0xF7;
    private final byte REPORT_INPUT_PINS = (byte) 0x5F;
    private final byte RESET_MICRO = (byte) 0x60;
    private final byte BLUETOOTH_RESET = (byte) 0x61;
    private final byte IS_ALIVE = (byte) 0x62;
    private final byte MUTE_FIRMATA = (byte) 0x64;
    private final byte UART_COMMAND = (byte) 0x65;
    private final byte UART_DATA = (byte) 0x66;

    private final byte CONFIGURATION_SHIELD_ID = (byte) 0x00;
    private final byte BT_CONNECTED = (byte) 0x01;
    private final byte QUERY_LIBRARY_VERSION = (byte) 0x03;
    private final byte LIBRARY_VERSION_RESPONSE = (byte) 0x01;
    private final byte IS_HARDWARE_CONNECTED_QUERY = (byte) 0x02;
    private final byte IS_CALLBACK_ENTERED = (byte) 0x03;
    private final byte IS_CALLBACK_EXITED = (byte) 0x04;

    private final Object sysexLock = new Object();
    private final Object arduinoCallbacksLock = new Object();
    private Thread exitingCallbacksThread, enteringCallbacksThread;
    private TimeOut callbacksTimeout;
    private long lastTimeCallbacksExited;
    private int sysexBytesCount = 0;

    Handler uiThreadHandler;

    public void resetMicro() {
        synchronized (sysexLock) {
            sysex(RESET_MICRO, new byte[]{});
        }
    }

    public boolean isVersionQueried() {
        return isVersionQueried;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getArduinoLibraryVersion() {
        return arduinoLibraryVersion;
    }

    private CopyOnWriteArrayList<ArduinoFirmataEventHandler> eventHandlers;
    private CopyOnWriteArrayList<ArduinoFirmataDataHandler> dataHandlers;
    private CopyOnWriteArrayList<ArduinoFirmataShieldFrameHandler> frameHandlers;
    private CopyOnWriteArrayList<FirmwareVersionQueryHandler> firmwareVersionQueryHandlers;
    private CopyOnWriteArrayList<ArduinoLibraryVersionChangeHandler> arduinoLibraryVersionChangeHandlers;
    private static Queue<ShieldFrame> queuedFrames;

    public void addEventHandler(ArduinoFirmataEventHandler handler) {
        if (handler != null && !eventHandlers.contains(handler))
            eventHandlers.add(handler);
    }

    public void removeEventHandler(ArduinoFirmataEventHandler handler) {
        if (handler != null && eventHandlers.contains(handler))
            eventHandlers.remove(handler);
    }

    public void addDataHandler(ArduinoFirmataDataHandler handler) {
        if (handler != null && !dataHandlers.contains(handler))
            dataHandlers.add(handler);
    }

    public void removeDataHandler(ArduinoFirmataDataHandler handler) {
        if (handler != null && dataHandlers.contains(handler))
            dataHandlers.remove(handler);
    }

    public void addShieldFrameHandler(ArduinoFirmataShieldFrameHandler handler) {
        if (handler != null && !frameHandlers.contains(handler))
            frameHandlers.add(handler);
    }

    public void removeShieldFrameHandler(
            ArduinoFirmataShieldFrameHandler handler) {
        if (handler != null && frameHandlers.contains(handler))
            frameHandlers.remove(handler);
    }

    public void addFirmwareVersionQueryHandler(
            FirmwareVersionQueryHandler handler) {
        if (handler != null && !firmwareVersionQueryHandlers.contains(handler))
            firmwareVersionQueryHandlers.add(handler);
    }

    public void addArduinoLibraryVersionQueryHandler(
            ArduinoLibraryVersionChangeHandler handler) {
        if (handler != null
                && !arduinoLibraryVersionChangeHandlers.contains(handler))
            arduinoLibraryVersionChangeHandlers.add(handler);
    }

    public void enableBootloaderMode() {
        isBootloader = true;
    }

    public void disableBootloaderMode() {
        isBootloader = false;
    }

    public boolean isBootloaderMode() {
        return isBootloader;
    }

    private int waitForData = 0;
    private byte executeMultiByteCommand = 0;
    private byte multiByteChannel = 0;
    private byte[] storedInputData = new byte[MAX_DATA_BYTES];
    private boolean parsingSysex = false;
    private int sysexBytesRead = 0;
    private int[] digitalOutputData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0};
    private int[] digitalInputData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0};
    private int[] analogInputData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0};
    private int majorVersion = 0;
    private int minorVersion = 0;
    private boolean isVersionQueried = false;
    private BluetoothService bluetoothService;
    private Context context;
    private boolean isBluetoothBufferWaiting;
    private boolean isUartBufferWaiting;
    private boolean isInACallback;

    public int getBTState() {
        return bluetoothService.getState();
    }

    public void resetProcessInput() {
        waitForData = 0;
        executeMultiByteCommand = 0;
        multiByteChannel = 0;
        storedInputData = new byte[MAX_DATA_BYTES];
        parsingSysex = false;
        sysexBytesRead = 0;
    }

    public boolean getCallbackStatus() {
        synchronized (arduinoCallbacksLock) {
            return isInACallback;
        }

    }

    public ArduinoFirmata(Context context) {
        uartBuffer = new LinkedBlockingQueue<Byte>();
        bluetoothBuffer = new LinkedBlockingQueue<Byte>();
        bluetoothService = new BluetoothService(context);
        eventHandlers = new CopyOnWriteArrayList<ArduinoFirmataEventHandler>();
        dataHandlers = new CopyOnWriteArrayList<ArduinoFirmataDataHandler>();
        frameHandlers = new CopyOnWriteArrayList<ArduinoFirmataShieldFrameHandler>();
        firmwareVersionQueryHandlers = new CopyOnWriteArrayList<FirmwareVersionQueryHandler>();
        arduinoLibraryVersionChangeHandlers = new CopyOnWriteArrayList<ArduinoLibraryVersionChangeHandler>();
        queuedFrames = new ConcurrentLinkedQueue<>();
        this.context = context;
        bluetoothService.addBluetoothServiceHandler(handler);
        uiThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void connect(BluetoothDevice device) {
        bluetoothService.connect(device);
    }

    public void enableReporting() {
        for (byte i = 0; i < 6; i++) {
            write(new byte[]{(byte) (REPORT_ANALOG | i), 1});
        }

        for (byte i = 0; i < 3; i++) {
            write(new byte[]{(byte) (REPORT_DIGITAL | i), 1});
        }
    }

    public void reportInputPinsValues() {
        synchronized (sysexLock) {
            sysex(REPORT_INPUT_PINS, new byte[]{});
        }
    }

    public void setAllPinsAsInput() {
        for (int i = 0; i < 20; i++) {
            pinMode(i, INPUT);
        }
    }

    public void respondToIsAlive() {
        synchronized (sysexLock) {
            sysex(IS_ALIVE, new byte[]{});
        }
    }

    public void notifyHardwareOfConnection() {
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, BT_CONNECTED));
    }

    public boolean isOpen() {
        return bluetoothService != null
                && bluetoothService.getState() == BluetoothService.STATE_CONNECTED;
    }

    public boolean close() {
        clearArduinoFirmataDataHandlers();
        clearArduinoFirmataShieldFrameHandlers();
        stopBuffersThreads();
        arduinoLibraryVersion = -1;
        if (callbacksTimeout != null) callbacksTimeout.stopTimer();
        if (exitingCallbacksThread != null && exitingCallbacksThread.isAlive())
            exitingCallbacksThread.interrupt();
        if (enteringCallbacksThread != null && enteringCallbacksThread.isAlive())
            enteringCallbacksThread.interrupt();
        if (bluetoothService != null && isOpen())
            bluetoothService.stopConnection();
//        queuedFrames = null;
        while(queuedFrames!=null&&!queuedFrames.isEmpty())queuedFrames.poll();
        isInACallback = false;

        return true;
    }

    public void stopBuffersThreads() {
        if (uartListeningThread != null) {
            uartListeningThread.stopRunning();
        }
        if (bluetoothBufferListeningThread != null) {
            bluetoothBufferListeningThread.stopRunning();
        }
    }

    public void write(byte[] writeData) {

        if (isOpen())
            bluetoothService.write(writeData);

    }

    public void reset() {
        write(new byte[]{SYSTEM_RESET});
    }

    public void sysex(byte command, byte[] bytes) {
        // http://firmata.org/wiki/V2.1ProtocolDetails#Sysex_Message_Format
        byte[] data = getByteArrayAs2SevenBitsBytesArray(bytes);
        if (data.length > 32)
            return;
        byte[] writeData = new byte[data.length + 3];
        writeData[0] = START_SYSEX;
        writeData[1] = command;
        for (int i = 0; i < data.length; i++) {
            writeData[i + 2] = (byte) (data[i] & 127); // 7bit
        }
        writeData[writeData.length - 1] = END_SYSEX;
        write(writeData);
    }

    private byte[] getByteAs2SevenBitsBytes(byte data) {
        byte[] temp = new byte[2];
        temp[0] = (byte) ((data & 0xFF) & 127);
        temp[1] = (byte) (((data & 0xFF) >> 7) & 127);
        return temp;
    }

    private byte[] getByteArrayAs2SevenBitsBytesArray(byte[] data) {
        byte[] temp = new byte[data.length * 2];
        for (int i = 0; i < temp.length; i += 2) {
            temp[i] = getByteAs2SevenBitsBytes(data[i / 2])[0];
            temp[i + 1] = getByteAs2SevenBitsBytes(data[i / 2])[1];
        }
        return temp;
    }

    public boolean digitalRead(int pin) {
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    public int analogRead(int pin) {
        return analogInputData[pin];
    }

    public void pinMode(int pin, byte mode) {
        byte[] writeData = {SET_PIN_MODE, (byte) pin, mode};
        write(writeData);
    }

    public void digitalWrite(int pin, boolean value) {
        byte portNumber = (byte) ((pin >> 3) & 0x0F);
        if (!value)
            digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
        else
            digitalOutputData[portNumber] |= (1 << (pin & 0x07));
        byte[] writeData = {SET_PIN_MODE, (byte) pin, OUTPUT,
                (byte) (DIGITAL_MESSAGE | portNumber),
                (byte) (digitalOutputData[portNumber] & 0x7F),
                (byte) (digitalOutputData[portNumber] >> 7)};
        write(writeData);
    }

    public void analogWrite(int pin, int value) {
        byte[] writeData = {SET_PIN_MODE, (byte) pin, PWM,
                (byte) (ANALOG_MESSAGE | (pin & 0x0F)), (byte) (value & 0x7F),
                (byte) (value >> 7)};
        write(writeData);
    }

    public void servoWrite(int pin, int angle) {
        byte[] writeData = {SET_PIN_MODE, (byte) pin, SERVO,
                (byte) (ANALOG_MESSAGE | (pin & 0x0F)), (byte) (angle & 0x7F),
                (byte) (angle >> 7)};
        write(writeData);
    }

    private void setDigitalInputs(int portNumber, int portData) {
        digitalInputData[portNumber] = portData;
        for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
            dataHandler.onDigital(portNumber, portData);
        }
    }

    private void setAnalogInput(int pin, int value) {
        analogInputData[pin] = value;
        pin = pin + 14; // for arduino uno analog pin mapping
        for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
            dataHandler.onAnalog(pin, value);
        }

    }

    private void setVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        for (FirmwareVersionQueryHandler handler : firmwareVersionQueryHandlers) {
            handler.onVersionReceived(minorVersion, majorVersion);
        }
    }

    private void processInput(byte inputData) {
        byte command;
        if (parsingSysex) {
            if (inputData == END_SYSEX) {
                parsingSysex = false;
                byte sysexCommand = storedInputData[0];
                if (sysexBytesRead > 0) {
                    byte[] sysexData = new byte[sysexBytesRead - 1];

                    System.arraycopy(storedInputData, 1, sysexData, 0,
                            sysexBytesRead - 1);

                    byte[] fixedSysexData = null;
                    if (sysexData.length % 2 == 0) {
                        fixedSysexData = new byte[sysexData.length / 2];
                        for (int i = 0; i < sysexData.length; i += 2) {
                            fixedSysexData[i / 2] = (byte) (sysexData[i] | (sysexData[i + 1] << 7));
                        }

                        if (sysexCommand == UART_DATA && fixedSysexData != null) {
                            for (byte b : fixedSysexData) {
                                uartBuffer.add(b);
                            }
                        } else if (sysexCommand == BLUETOOTH_RESET) {
                            if (!isBootloader) {
                                byte randomVal=(byte)(Math.random()*255);
                                byte complement=(byte)(255-randomVal&0xFF);
                                synchronized (sysexLock) {
                                    sysex(BLUETOOTH_RESET, new byte[]{0x01,randomVal,complement});
                                }
                                close();
                            }
                        } else if (sysexCommand == IS_ALIVE) {
                            respondToIsAlive();
                        } else
                            for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
                                dataHandler.onSysex(sysexCommand, sysexData);
                            }
                    }
                } else {
                    for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
                        dataHandler.onSysex(sysexCommand, new byte[]{});
                    }
                }

            } else {
                if (sysexBytesRead < storedInputData.length) {
                    storedInputData[sysexBytesRead] = inputData;
                    sysexBytesRead++;
                }
            }
        } else if (waitForData > 0 && (int) (inputData & 0xFF) < 128) {
            waitForData--;
            storedInputData[waitForData] = inputData;
            if (executeMultiByteCommand != 0 && waitForData == 0) {
                switch (executeMultiByteCommand) {
                    case DIGITAL_MESSAGE:
                        setDigitalInputs(multiByteChannel,
                                (storedInputData[0] << 7) + storedInputData[1]);
                        break;
                    case ANALOG_MESSAGE:
                        setAnalogInput(multiByteChannel, (storedInputData[0] << 7)
                                + storedInputData[1]);
                        break;
                    case REPORT_VERSION:
                        setVersion(storedInputData[0], storedInputData[1]);
                        isVersionQueried = true;
                        break;
                }
            }
        } else {
            if ((int) (inputData & 0xFF) < 0xF0) {
                command = (byte) (inputData & 0xF0);
                multiByteChannel = (byte) (inputData & 0x0F);
            } else {
                command = inputData;
            }
            switch (command) {
                case START_SYSEX:
                    parsingSysex = true;
                    sysexBytesRead = 0;
                    break;
                case DIGITAL_MESSAGE:
                case ANALOG_MESSAGE:
                case REPORT_VERSION:
                    waitForData = 2;
                    executeMultiByteCommand = command;
                    break;
            }
        }
    }

    private void resetBluetoothSysex(byte input) {
        switch (sysexBytesCount) {
            case 0:
                if (input != START_SYSEX) {
                    sysexBytesCount = 0;
                    return;
                } else
                    sysexBytesCount++;
                break;
            case 1:
                if (input != BLUETOOTH_RESET) {
                    sysexBytesCount = 0;
                    return;
                } else
                    sysexBytesCount++;
                break;
            case 2:
                if (input != END_SYSEX) {
                    sysexBytesCount = 0;
                    return;
                } else
                    synchronized (sysexLock) {
                        sysex(BLUETOOTH_RESET, new byte[]{0x00});
                    }
                sysexBytesCount = 0;
                break;

            default:
                sysexBytesCount = 0;
                return;
        }

    }

    private void printFrameToLog(byte[] frame, String tag) {
        String s = "";
        for (byte b : frame) {
            if ((Integer.toHexString(b).length() < 2))
                s += "0" + Integer.toHexString(b) + " ";
            else if ((Integer.toHexString(b).length() == 2))
                s += Integer.toHexString(b) + " ";
            else {
                String temp = Integer.toHexString(b);
                temp = temp.substring(temp.length() - 2);
                s += temp + " ";
            }
        }
        Log.d(tag, s);
    }

    public void queueShieldFrame(ShieldFrame frame) {
        if (queuedFrames != null) {
            queuedFrames.add(frame);
            callbackEntered();
        }
    }

    public void sendShieldFrame(ShieldFrame frame, boolean waitIfInACallback) {
        if (!waitIfInACallback) {
            sendFrame(frame);
            return;
        }

        boolean inACallback = false;

        synchronized (arduinoCallbacksLock) {
            inACallback = isInACallback;
        }

        if (inACallback) {
            if (queuedFrames == null)
                queuedFrames = new ConcurrentLinkedQueue<>();
            queuedFrames.add(frame);
        } else {
            if (queuedFrames != null) {
                if (queuedFrames.isEmpty()) {
                    sendFrame(frame);
                } else {
                    queuedFrames.add(frame);
                }
            } else {
                sendFrame(frame);
            }
        }
    }

    public void sendShieldFrame(ShieldFrame frame) {
        sendShieldFrame(frame, false);
    }

    private void sendFrame(ShieldFrame frame) {
        if (isBootloader||frame==null)
            return;
        byte[] frameBytes = frame.getAllFrameAsBytes();
        int maxShieldFrameBytes = (MAX_OUTPUT_BYTES - 3) / 2;// The 3 is for
        // StartSysex,
        // EndSysex and
        // Uart_data
        ArrayList<byte[]> subArrays = new ArrayList<byte[]>();
        for (int i = 0; i < frameBytes.length; i += maxShieldFrameBytes) {
            byte[] subArray = (i + maxShieldFrameBytes > frameBytes.length) ? ArrayUtils
                    .copyOfRange(frameBytes, i, frameBytes.length) : ArrayUtils
                    .copyOfRange(frameBytes, i, i + maxShieldFrameBytes);
            subArrays.add(subArray);
        }
        synchronized (sysexLock) {
            for (byte[] sub : subArrays)
                sysex(UART_DATA, sub);
        }
        printFrameToLog(frameBytes, "Sent");
    }

    public void prepareAppForSendingFirmware() {
        muteFirmata();
        clearAllBuffers();
        resetProcessInput();
        enableBootloaderMode();
    }

    public void returnAppToNormal() {
        disableBootloaderMode();
        clearAllBuffers();
        unMuteFirmata();
        enableReporting();
        setAllPinsAsInput();
        reportInputPinsValues();
        queryFirmwareVersion();
        respondToIsAlive();
        notifyHardwareOfConnection();
        queryLibraryVersion();
    }

    private void muteFirmata() {
        synchronized (sysexLock) {
            sysex(MUTE_FIRMATA, new byte[]{1});
        }
    }

    private void unMuteFirmata() {
        synchronized (sysexLock) {
            sysex(MUTE_FIRMATA, new byte[]{0});
        }
    }

    private void queryFirmwareVersion() {
        write(new byte[]{REPORT_VERSION});
    }

    private void queryLibraryVersion() {
        sendShieldFrame(new ShieldFrame(CONFIGURATION_SHIELD_ID, QUERY_LIBRARY_VERSION));
    }

    private void clearAllBuffers() {
        bluetoothBuffer.clear();
        uartBuffer.clear();
    }

    public BluetoothService getBTService() {
        return bluetoothService;
    }

    private void onClose(final boolean isManually) {
        for (final ArduinoFirmataEventHandler eventHandler : eventHandlers) {
            uiThreadHandler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    if (eventHandler != null)
                        eventHandler.onClose(isManually);
                }
            });

        }
    }

    private void onConnect() {
        for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
            if (eventHandler != null)
                eventHandler.onConnect();
        }
    }

    BluetoothServiceHandler handler = new BluetoothServiceHandler() {

        @Override
        public void onDataReceived(byte[] bytes, int length) {
            // TODO Auto-generated method stub
            for (int i = 0; i < length; i++) {
                if (i < bytes.length)
                    bluetoothBuffer.add(bytes[i]);
                else
                    break;
            }

        }

        @Override
        public void onStateChanged(final int state, final boolean isManually) {
            // TODO Auto-generated method stub
            if (state == BluetoothService.STATE_NONE) {
                onClose(isManually);
            }
        }

        @Override
        public void onDataWritten(byte[] bytes) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnected(final BluetoothDevice device) {
            // TODO Auto-generated method stub

            initFirmata(device);

        }

        @Override
        public void onError(final String error) {
            // TODO Auto-generated method stub
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                }
            });

        }
    };

    private void initFirmata(final BluetoothDevice device) {
        isBluetoothBufferWaiting = false;
        isUartBufferWaiting = false;
        stopBuffersThreads();
        clearAllBuffers();
        resetProcessInput();
        isVersionQueried = false;
        bluetoothBufferListeningThread = new BluetoothBufferListeningThread();
        uartListeningThread = new UartListeningThread();
        while (!isBluetoothBufferWaiting)
            ;
        while (!isUartBufferWaiting)
            ;

        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                enableReporting();
                setAllPinsAsInput();
                reportInputPinsValues();
                onConnect();
                respondToIsAlive();
                queryFirmwareVersion();
                notifyHardwareOfConnection();
                queryLibraryVersion();
            }
        });
    }

    private byte readByteFromUartBuffer() throws InterruptedException,
            ShieldFrameNotComplete {
        if (ShieldFrameTimeout != null && ShieldFrameTimeout.isTimeout())
            throw new ShieldFrameNotComplete();
        isUartBufferWaiting = true;
        byte temp = uartBuffer.take().byteValue();
        if (ShieldFrameTimeout != null)
            ShieldFrameTimeout.resetTimer();
        return temp;
    }

    private byte readByteFromBluetoothBuffer() throws InterruptedException {
        isBluetoothBufferWaiting = true;
        return bluetoothBuffer.take().byteValue();
    }

    public void clearAllHandlers() {
        eventHandlers.clear();
        dataHandlers.clear();
        frameHandlers.clear();
    }

    private void callbackEntered() {
        if (enteringCallbacksThread != null && enteringCallbacksThread.isAlive())
            return;
        enteringCallbacksThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (arduinoCallbacksLock) {
                        isInACallback = true;
                }

                if (callbacksTimeout == null || (callbacksTimeout != null && !callbacksTimeout.isAlive())) {
                    callbacksTimeout = new TimeOut(5, new TimeOut.TimeoutHandler() {
                        @Override
                        public void onTimeout() {
                                callbackExited();
                        }

                        @Override
                        public void onTick(int secondsLeft) {

                        }
                    });
                } else
                    callbacksTimeout.resetTimer();


            }
        });
        enteringCallbacksThread.start();

    }

    private void callbackExited() {
        synchronized (arduinoCallbacksLock) {
            isInACallback = false;
            lastTimeCallbacksExited= SystemClock.elapsedRealtime();
        }
        if (callbacksTimeout != null && !callbacksTimeout.isAlive())callbacksTimeout.stopTimer();
        if (exitingCallbacksThread != null && exitingCallbacksThread.isAlive())
            return;
        exitingCallbacksThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sent = false;
                while (queuedFrames != null && !queuedFrames.isEmpty()) {
                    sent = false;
//                    boolean isInCallback;
                    synchronized (arduinoCallbacksLock) {
//                        isInCallback = isInACallback;
                        if (!isInACallback&&lastTimeCallbacksExited!=0&&(SystemClock.elapsedRealtime()-lastTimeCallbacksExited>200)) {
                            sendFrame(queuedFrames.poll());
                            sent = true;
                        }
                    }

//                    if (!isInCallback&&lastTimeCallbacksExited!=0&&(System.currentTimeMillis()-lastTimeCallbacksExited>100)) {
//                        sendFrame(queuedFrames.poll());
//                        sent = true;
//                    }
                    
                    if (sent)
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                        }
                }
            }
        });
        exitingCallbacksThread.start();
    }

    public void clearArduinoFirmataEventHandlers() {
        eventHandlers.clear();
    }

    public void clearArduinoFirmataShieldFrameHandlers() {
        frameHandlers.clear();
    }

    public void clearArduinoFirmataDataHandlers() {
        dataHandlers.clear();
    }

    private class UartListeningThread extends Thread {
        public UartListeningThread() {
            start();
        }

        private void stopRunning() {
            if (this.isAlive())
                this.interrupt();
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (!this.isInterrupted()) {
                try {
                    while ((readByteFromUartBuffer()) != ShieldFrame.START_OF_FRAME)
                        ;
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = new TimeOut(1);
                    int tempArduinoLibVersion = readByteFromUartBuffer();
                    byte shieldId = readByteFromUartBuffer();
                    boolean found = false;
                    for (UIShield shield : UIShield.values()) {
                        if (shieldId == shield.getId() || shieldId == CONFIGURATION_SHIELD_ID)
                            found = true;
                    }
                    if (!found) {
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        uartBuffer.clear();
                        continue;
                    }
                    byte instanceId = readByteFromUartBuffer();
                    byte functionId = readByteFromUartBuffer();
                    ShieldFrame frame = new ShieldFrame(shieldId, instanceId,
                            functionId);
                    int argumentsNumber = readByteFromUartBuffer() & 0xFF;
                    int argumentsNumberVerification = (255 - (readByteFromUartBuffer() & 0xFF));
                    if (argumentsNumber != argumentsNumberVerification) {
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        uartBuffer.clear();
                        continue;
                    }
                    for (int i = 0; i < argumentsNumber; i++) {
                        int length = readByteFromUartBuffer() & 0xff;
                        int lengthVerification = (255 - (readByteFromUartBuffer() & 0xFF));
                        if (length != lengthVerification || length <= 0) {
                            if (ShieldFrameTimeout != null)
                                ShieldFrameTimeout.stopTimer();
                            uartBuffer.clear();
                            continue;
                        }
                        byte[] data = new byte[length];
                        for (int j = 0; j < length; j++) {
                            data[j] = readByteFromUartBuffer();
                        }
                        frame.addArgument(data);
                    }
                    if ((readByteFromUartBuffer()) != ShieldFrame.END_OF_FRAME) {
                        if (ShieldFrameTimeout != null)
                            ShieldFrameTimeout.stopTimer();
                        uartBuffer.clear();
                        continue;
                    }
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    if (arduinoLibraryVersion != tempArduinoLibVersion) {
                        arduinoLibraryVersion = tempArduinoLibVersion;
                        for (ArduinoLibraryVersionChangeHandler handler : arduinoLibraryVersionChangeHandlers) {
                            handler.onArduinoLibraryVersionChange(arduinoLibraryVersion);
                        }
                    }
                    printFrameToLog(frame.getAllFrameAsBytes(), "Rec");
                    if (shieldId == CONFIGURATION_SHIELD_ID) {
                        //1Sheeld configration from the library
                        if (functionId == LIBRARY_VERSION_RESPONSE) {

                        } else if (functionId == IS_HARDWARE_CONNECTED_QUERY) {
                            notifyHardwareOfConnection();
                        } else if (functionId == IS_CALLBACK_ENTERED) {
                            callbackEntered();
                        } else if (functionId == IS_CALLBACK_EXITED) {
                            callbackExited();
                        }
                    } else for (ArduinoFirmataShieldFrameHandler frameHandler : frameHandlers) {
                        frameHandler.onNewShieldFrameReceived(frame);
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (ShieldFrameNotComplete e) {
                    if (ShieldFrameTimeout != null)
                        ShieldFrameTimeout.stopTimer();
                    ShieldFrameTimeout = null;
                    // uartBuffer.clear();
                    continue;
                }
            }
        }
    }

    private class ShieldFrameNotComplete extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

    }

    private class BluetoothBufferListeningThread extends Thread {
        public BluetoothBufferListeningThread() {
            // TODO Auto-generated constructor stub
            start();
        }

        private void stopRunning() {
            if (this.isAlive())
                this.interrupt();
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            byte input;
            while (!this.isInterrupted()) {

                try {
                    input = readByteFromBluetoothBuffer();
                    if (!isBootloader)
                        processInput(input);
                    else {
                        resetBluetoothSysex(input);
                    }
                } catch (InterruptedException e) {
                    return;
                }

            }
        }
    }

}