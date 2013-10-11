package com.integreight.firmatabluetooth;



import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


public class ArduinoFirmata{
    public final static String VERSION = "0.2.0";
    public final static String TAG = "ArduinoFirmata";

    public static final byte INPUT  = 0;
    public static final byte OUTPUT = 1;
    public static final byte ANALOG = 2;
    public static final byte PWM    = 3;
    public static final byte SERVO  = 4;
    public static final byte SHIFT  = 5;
    public static final byte I2C    = 6;
    public static final boolean LOW   = false;
    public static final boolean HIGH  = true;
    
    public static final int A0=14;
    public static final int A1=15;
    public static final int A2=16;
    public static final int A3=15;
    public static final int A4=17;
    public static final int A5=18;
    
    private final char MAX_DATA_BYTES  = 256;
    private final byte DIGITAL_MESSAGE = (byte)0x90;
    private final byte ANALOG_MESSAGE  = (byte)0xE0;
    private final byte REPORT_ANALOG   = (byte)0xC0;
    private final byte REPORT_DIGITAL  = (byte)0xD0;
    private final byte SET_PIN_MODE    = (byte)0xF4;
    private final byte REPORT_VERSION  = (byte)0xF9;
    private final byte SYSTEM_RESET    = (byte)0xFF;
    private final byte START_SYSEX     = (byte)0xF0;
    private final byte END_SYSEX       = (byte)0xF7;
    private final byte UART_COMMAND    = (byte)0x65;
    private final byte UART_DATA       = (byte)0x66;
    private final byte PULSE_IN_INIT    = (byte)0x67;
    private final byte PULSE_IN_DATA       = (byte)0x68;

    private final byte UART_BEGIN       = (byte)0x01;
    private final byte UART_END       = (byte)0x00;
    
    boolean isUartInit=false;
    

    public boolean isUartInit() {
		return isUartInit;
	}

	public static final int MESSAGE_DEVICE_NAME = BluetoothService.MESSAGE_DEVICE_NAME;
    private List<ArduinoFirmataEventHandler> eventHandlers;
    private List<ArduinoFirmataDataHandler> dataHandlers;
    public void addEventHandler(ArduinoFirmataEventHandler handler){
        if(!eventHandlers.contains(handler))eventHandlers.add(handler);
    }
    public void addDataHandler(ArduinoFirmataDataHandler handler){
        if(!dataHandlers.contains(handler))dataHandlers.add(handler);
    }

    private int waitForData = 0;
    private byte executeMultiByteCommand = 0;
    private byte multiByteChannel = 0;
    private byte[] storedInputData = new byte[MAX_DATA_BYTES];
    private boolean parsingSysex = false;
    private int sysexBytesRead = 0;
    private int[] digitalOutputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int[] digitalInputData  = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int[] analogInputData   = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private int majorVersion = 0;
    private int minorVersion = 0;
    private BluetoothService bluetoothService;
    private Context context;
    public String getBoardVersion(){
        return String.valueOf(majorVersion)+"."+String.valueOf(minorVersion);
    }
    
    public int getBTState() {
        return bluetoothService.getState();
    }

    public ArduinoFirmata(Context context){
        bluetoothService=new BluetoothService(context, mHandler);
        eventHandlers=new ArrayList<ArduinoFirmataEventHandler>();
        dataHandlers=new ArrayList<ArduinoFirmataDataHandler>();
        this.context=context;
    }

    public void connect(BluetoothDevice device){
        bluetoothService.connect(device);
    }
    
    private void enableReporting(){
        for (byte i = 0; i < 6; i++) {
            write((byte)(REPORT_ANALOG | i));
            write((byte)1);
        }

        for (byte i = 0; i < 2; i++) {
            write((byte)(REPORT_DIGITAL | i));
            write((byte)1);
        }
    }
    
    private void setAllPinsAsOutput(){
    	for(int i=0;i<20;i++){
    		pinMode(i, OUTPUT);
    	}
    }

    public boolean isOpen(){
        return bluetoothService!=null&&bluetoothService.getState()==BluetoothService.STATE_CONNECTED;
    }

    public boolean close(){
        	if(bluetoothService!=null)bluetoothService.stopConnection();
//        	for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
//        		eventHandler.onClose();
//    		}
            this.bluetoothService = null;
            return true;
    }

    public void write(byte[] writeData){

    if(isOpen()) bluetoothService.write(writeData);

    }

    public void write(byte writeData){
        byte[] _writeData = {(byte)writeData};
        write(_writeData);
    }

    public void reset(){
        write(SYSTEM_RESET);
    }

    public void sysex(byte command, byte[] data){
     	 // http://firmata.org/wiki/V2.1ProtocolDetails#Sysex_Message_Format
        if(data.length > 32) return;
        byte[] writeData = new byte[data.length+3];
        writeData[0] = START_SYSEX;
        writeData[1] = command;
        for(int i = 0; i < data.length; i++){
            writeData[i+2] = (byte)(data[i] & 127); // 7bit
        }
        writeData[writeData.length-1] = END_SYSEX;
        write(writeData);
    }
    
    public void sendUart(char[] data){
    	if(!isUartInit)return;
    	byte[] byteArray=new byte[data.length*2];
    	for (int i = 0; i < byteArray.length; i+=2) {
    		byteArray[i]=(byte) (data[i/2] & 0x127);
    		byteArray[i+1]=(byte) (data[i/2]>> 7 & 0x127);
		}
    	sysex(UART_DATA, byteArray);
    }
    
    public void initUart(BaudRate baud){
    	sysex(UART_COMMAND, new byte[]{UART_BEGIN,baud.getValue()});
    	//sysex(UART_COMMAND, new byte[]{baud.getValue()});
    	isUartInit=true;
    }
    
    public void initUart(){
    	initUart(BaudRate._57600);
    }
    
    public void disableUart(){
    	sysex(UART_COMMAND, new byte[]{UART_END});
    	isUartInit=false;
    	
    }

    public boolean digitalRead(int pin) {
        return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
    }

    public int analogRead(int pin) {
        return analogInputData[pin];
    }

    public void pinMode(int pin, byte mode) {
        byte[] writeData = {SET_PIN_MODE, (byte)pin, mode};
        write(writeData);
    }

    public void digitalWrite(int pin, boolean value) {
        byte portNumber = (byte)((pin >> 3) & 0x0F);
        if (!value) digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
        else digitalOutputData[portNumber] |= (1 << (pin & 0x07));
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, OUTPUT,
            (byte)(DIGITAL_MESSAGE | portNumber),
            (byte)(digitalOutputData[portNumber] & 0x7F),
            (byte)(digitalOutputData[portNumber] >> 7)
        };
        write(writeData);
    }

    public void analogWrite(int pin, int value) {
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, PWM,
            (byte)(ANALOG_MESSAGE | (pin & 0x0F)),
            (byte)(value & 0x7F),
            (byte)(value >> 7)
        };
        write(writeData);
    }

    public void servoWrite(int pin, int angle){
        byte[] writeData = {
            SET_PIN_MODE, (byte)pin, SERVO,
            (byte)(ANALOG_MESSAGE | (pin & 0x0F)),
            (byte)(angle & 0x7F),
            (byte)(angle >> 7)
        };
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
        pin=pin+14; //for arduino uno analog pin mapping
        for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
        	dataHandler.onAnalog(pin, value);
		}
        
    }

    private void setVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    private void processInput(byte inputData){
        byte command;
        if(parsingSysex){
            if(inputData == END_SYSEX){
                parsingSysex = false;
                byte sysexCommand = storedInputData[0];
                byte[] sysexData = new byte[sysexBytesRead-1];
                
                System.arraycopy(storedInputData, 1, sysexData, 0, sysexBytesRead-1);
                
                byte[] fixedSysexData = null;
                if(sysexData.length%2==0){
                	fixedSysexData=new byte[sysexData.length/2];
                	for(int i=0;i<sysexData.length;i+=2){
                		fixedSysexData[i/2]=(byte) (sysexData[i]|(sysexData[i+1]<<7));
                	}
                }
                for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
                	dataHandler.onSysex(sysexCommand, sysexData);
                	if(sysexCommand==UART_DATA&&fixedSysexData!=null) dataHandler.onUartReceive(fixedSysexData);
        		}
            }
            else{
                if(sysexBytesRead < storedInputData.length){
                    storedInputData[sysexBytesRead] = inputData;
                    sysexBytesRead++;
                }
            }
        }
        else if(waitForData > 0 && inputData < 128){
            waitForData--;
            storedInputData[waitForData] = inputData;
            if(executeMultiByteCommand != 0 && waitForData == 0){
                switch(executeMultiByteCommand){
                case DIGITAL_MESSAGE:
                    setDigitalInputs(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
                    break;
                case ANALOG_MESSAGE:
                    setAnalogInput(multiByteChannel, (storedInputData[0] << 7) + storedInputData[1]);
                    break;
                case REPORT_VERSION:
                    setVersion(storedInputData[1], storedInputData[0]);
                    break;
                }
            }
        }
        else {
            if(inputData < 0xF0){
                command = (byte)(inputData & 0xF0);
                multiByteChannel = (byte)(inputData & 0x0F);
            }
            else{
                command = inputData;
            }
            switch(command){
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
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:

                    break;
                case BluetoothService.STATE_CONNECTING:

                    break;
                case BluetoothService.STATE_NONE:
                	for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
                		eventHandler.onClose(msg.arg2==1);
            		}
                	
                    break;
                }
                break;
            case BluetoothService.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                break;
            case BluetoothService.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer;
                if(msg.arg1 > 0){
                    for(int i = 0; i < msg.arg1; i++){
                        processInput(readBuf[i]);
                    }
                }
                break;
            case BluetoothService.MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	enableReporting();
            	setAllPinsAsOutput();
            	for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
            		eventHandler.onConnect();
        		}
            	
                String mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                Toast.makeText(context, "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                
//                Message activityMsg = activityHandler.obtainMessage(ArduinoFirmata.MESSAGE_DEVICE_NAME);
//                Bundle bundle = new Bundle();
//                bundle.putString(BluetoothService.DEVICE_NAME, mConnectedDeviceName);
//                activityMsg.setData(bundle);
//                mHandler.sendMessage(activityMsg);
//                activityHandler.sendMessage(activityMsg);
                
                break;
            case BluetoothService.MESSAGE_TOAST:
                Toast.makeText(context, msg.getData().getString(BluetoothService.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
    
    public static enum BaudRate{
        _1200((byte)0x00),
        _2400((byte)0x01),
        _4800((byte)0x02),
        _9600((byte)0x03),
        _14400((byte)0x04),
        _19200((byte)0x05),
        _28800((byte)0x06),
        _38400((byte)0x07),
        _57600((byte)0x08),
        _115200((byte)0x09);
        
        byte value;
        BaudRate(byte value){
        	this.value=value;
        }
        
        public byte getValue(){
        	return value;
        }
        
    }

}