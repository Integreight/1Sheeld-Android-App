package com.integreight.firmatabluetooth;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.integreight.firmatabluetooth.BluetoothService.BluetoothServiceHandler;
import com.integreight.onesheeld.enums.UIShield;
import com.integreight.onesheeld.utils.ArrayUtils;

public class ArduinoFirmata {
	public final static String VERSION = "0.2.0";
	public final static String TAG = "ArduinoFirmata";

	LinkedBlockingQueue<Byte> uartBuffer = new LinkedBlockingQueue<Byte>();
	LinkedBlockingQueue<Byte> bluetoothBuffer = new LinkedBlockingQueue<Byte>();
	UartListeningThread uartListeningThread;
	BluetoothBufferListeningThread bluetoothBufferListeningThread;
	boolean isBootloader = false;

	public static final byte INPUT = 0;
	public static final byte OUTPUT = 1;
	public static final byte ANALOG = 2;
	public static final byte PWM = 3;
	public static final byte SERVO = 4;
	public static final byte SHIFT = 5;
	public static final byte I2C = 6;
	public static final boolean LOW = false;
	public static final boolean HIGH = true;

	public static final int A0 = 14;
	public static final int A1 = 15;
	public static final int A2 = 16;
	public static final int A3 = 15;
	public static final int A4 = 17;
	public static final int A5 = 18;

	public static int arduinoLibraryVersion=-1;

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
	private final byte RESET_MICRO = (byte) 0x60;
	private final byte BLUETOOTH_RESET = (byte) 0x61;
	// private final byte IS_ALIVE = (byte) 0x62;
	//private final byte FIRMWARE_VERSION_QUERY = (byte) 0x63; //Deprecated
	private final byte MUTE_FIRMATA = (byte) 0x64;
	private final byte UART_COMMAND = (byte) 0x65;
	private final byte UART_DATA = (byte) 0x66;
	// private final byte PULSE_IN_INIT = (byte)0x67;
	// private final byte PULSE_IN_DATA = (byte)0x68;

	private final byte UART_BEGIN = (byte) 0x01;
	private final byte UART_END = (byte) 0x00;

	Handler uiThreadHandler;

	boolean isUartInit = false;

	public boolean isUartInit() {
		return isUartInit;
	}
	
	public void resetMicro(){
		sysex(RESET_MICRO, new byte[]{});
	}
	
	public boolean isVersionQueried(){
		return isVersionQueried;
	}
	
	public int getMajorVersion(){
		return majorVersion;
	}

	public int getMinorVersion(){
		return minorVersion;
	}

	public int getArduinoLibraryVersion(){
		return arduinoLibraryVersion;
	}
	// public static final int MESSAGE_DEVICE_NAME =
	// BluetoothService.MESSAGE_DEVICE_NAME;
	private CopyOnWriteArrayList<ArduinoFirmataEventHandler> eventHandlers;
	private CopyOnWriteArrayList<ArduinoFirmataDataHandler> dataHandlers;
	private CopyOnWriteArrayList<ArduinoFirmataShieldFrameHandler> frameHandlers;
	private CopyOnWriteArrayList<ArduinoVersionQueryHandler> versionQueryHandlers;

	public void addEventHandler(ArduinoFirmataEventHandler handler) {
		if (handler != null && !eventHandlers.contains(handler))
			eventHandlers.add(handler);
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

	public void addVersionQueryHandler(ArduinoVersionQueryHandler handler) {
		if (handler != null && !versionQueryHandlers.contains(handler))
			versionQueryHandlers.add(handler);
	}

	private int waitForData = 0;
	private byte executeMultiByteCommand = 0;
	private byte multiByteChannel = 0;
	private byte[] storedInputData = new byte[MAX_DATA_BYTES];
	private boolean parsingSysex = false;
	private int sysexBytesRead = 0;
	private int[] digitalOutputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0 };
	private int[] digitalInputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0 };
	private int[] analogInputData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0 };
	private int majorVersion = 0;
	private int minorVersion = 0;
	private boolean isVersionQueried=false;
	private BluetoothService bluetoothService;
	private Context context;

//	public String getBoardVersion() {
//		return String.valueOf(majorVersion) + "."
//				+ String.valueOf(minorVersion);
//	}

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

	public ArduinoFirmata(Context context) {
		bluetoothService = new BluetoothService(context);
		eventHandlers = new CopyOnWriteArrayList<ArduinoFirmataEventHandler>();
		dataHandlers = new CopyOnWriteArrayList<ArduinoFirmataDataHandler>();
		frameHandlers = new CopyOnWriteArrayList<ArduinoFirmataShieldFrameHandler>();
		versionQueryHandlers = new CopyOnWriteArrayList<ArduinoVersionQueryHandler>();
		this.context = context;
		bluetoothService.addBluetoothServiceHandler(handler);
		uiThreadHandler = new Handler(Looper.getMainLooper());
	}

	public void connect(BluetoothDevice device) {
		bluetoothService.connect(device);
	}

	public void enableReporting() {
		for (byte i = 0; i < 6; i++) {
			write((byte) (REPORT_ANALOG | i));
			write((byte) 1);
		}

		for (byte i = 0; i < 3; i++) {
			write((byte) (REPORT_DIGITAL | i));
			write((byte) 1);
		}
	}

	public void setAllPinsAsInput() {
		for (int i = 0; i < 20; i++) {
			pinMode(i, INPUT);
		}
	}

	public boolean isOpen() {
		return bluetoothService != null
				&& bluetoothService.getState() == BluetoothService.STATE_CONNECTED;
	}

	public boolean close() {
		// clearAllHandlers();
		clearArduinoFirmataDataHandlers();
		clearArduinoFirmataShieldFrameHandlers();
		stopBuffersThreads();
		if (bluetoothService != null && isOpen())
			bluetoothService.stopConnection();

		// for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
		// eventHandler.onClose();
		// }
		// this.bluetoothService = null;
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

	public void write(byte writeData) {
		// byte[] _writeData = {(byte)writeData};
		if (isOpen())
			bluetoothService.write(writeData);
	}

	public void reset() {
		write(SYSTEM_RESET);
	}

	public void sysex(byte command, byte[] data) {
		// http://firmata.org/wiki/V2.1ProtocolDetails#Sysex_Message_Format
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
		temp[0] = (byte) (data & 127);
		temp[1] = (byte) (data >> 7 & 127);
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

	public void initUart(BaudRate baud) {
		sysex(UART_COMMAND, new byte[] { UART_BEGIN, baud.getValue() });
		isUartInit = true;
	}

	public void initUart() {
		initUart(BaudRate._57600);
	}

	public void disableUart() {
		sysex(UART_COMMAND, new byte[] { UART_END });
		isUartInit = false;

	}

	public boolean digitalRead(int pin) {
		return ((digitalInputData[pin >> 3] >> (pin & 0x07)) & 0x01) > 0;
	}

	public int analogRead(int pin) {
		return analogInputData[pin];
	}

	public void pinMode(int pin, byte mode) {
		byte[] writeData = { SET_PIN_MODE, (byte) pin, mode };
		write(writeData);
	}

	public void digitalWrite(int pin, boolean value) {
		byte portNumber = (byte) ((pin >> 3) & 0x0F);
		if (!value)
			digitalOutputData[portNumber] &= ~(1 << (pin & 0x07));
		else
			digitalOutputData[portNumber] |= (1 << (pin & 0x07));
		byte[] writeData = { SET_PIN_MODE, (byte) pin, OUTPUT,
				(byte) (DIGITAL_MESSAGE | portNumber),
				(byte) (digitalOutputData[portNumber] & 0x7F),
				(byte) (digitalOutputData[portNumber] >> 7) };
		write(writeData);
	}

	public void analogWrite(int pin, int value) {
		byte[] writeData = { SET_PIN_MODE, (byte) pin, PWM,
				(byte) (ANALOG_MESSAGE | (pin & 0x0F)), (byte) (value & 0x7F),
				(byte) (value >> 7) };
		write(writeData);
	}

	public void servoWrite(int pin, int angle) {
		byte[] writeData = { SET_PIN_MODE, (byte) pin, SERVO,
				(byte) (ANALOG_MESSAGE | (pin & 0x0F)), (byte) (angle & 0x7F),
				(byte) (angle >> 7) };
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
		for (ArduinoVersionQueryHandler handler : versionQueryHandlers) {
			handler.onVersionReceived(minorVersion,
					majorVersion);
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
						}
						
//						if (sysexCommand == FIRMWARE_VERSION_QUERY) {
//							if (sysexData.length >= 2) {
//								setVersion(sysexData[1], sysexData[0]);
//								for (ArduinoVersionQueryHandler handler : versionQueryHandlers) {
//									handler.onVersionReceived(minorVersion,
//											majorVersion);
//								}
//							}
//						}
						if (sysexCommand == BLUETOOTH_RESET) {
							if(!isBootloader){
							sysex(BLUETOOTH_RESET, new byte[]{0x01});
							close();
							}
						}

						for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
							dataHandler.onSysex(sysexCommand, sysexData);
						}
					}
				} else {
					for (ArduinoFirmataDataHandler dataHandler : dataHandlers) {
						dataHandler.onSysex(sysexCommand, new byte[] {});
					}
				}

			} else {
				if (sysexBytesRead < storedInputData.length) {
					storedInputData[sysexBytesRead] = inputData;
					sysexBytesRead++;
				}
			}
		} else if (waitForData > 0 && (int)(inputData&0xFF) < 128) {
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
					isVersionQueried=true;
					break;
				}
			}
		} else {
			if ((int)(inputData&0xFF) < 0xF0) {
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

	private void printFrameToLog(byte[] frame) {
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
		Log.d("frame", s);
	}

	public synchronized void sendShieldFrame(ShieldFrame frame) {
		if (!isUartInit||isBootloader)
			return;
		byte[] frameBytes = frame.getAllFrameAsBytes();
		printFrameToLog(frameBytes);
		int maxShieldFrameBytes = (MAX_OUTPUT_BYTES - 3) / 2;// The 3 is for
																// StartSysex,
																// EndSysex and
																// Uart_data
		for (int i = 0; i < frameBytes.length; i += maxShieldFrameBytes) {
			byte[] subArray = (i + maxShieldFrameBytes > frameBytes.length) ? ArrayUtils
					.copyOfRange(frameBytes, i, frameBytes.length) : ArrayUtils
					.copyOfRange(frameBytes, i, i + maxShieldFrameBytes);
			sysex(UART_DATA, getByteArrayAs2SevenBitsBytesArray(subArray));
		}
	}

	public void prepareAppForSendingFirmware() {
		muteFirmata();
		clearAllBuffers();
		resetProcessInput();
		isBootloader = true;
	}

	public void returnAppToNormal() {
		isBootloader = false;
		clearAllBuffers();
		unMuteFirmata();
	}

	private void muteFirmata() {
		sysex(MUTE_FIRMATA, new byte[] { 1 });
	}

	private void unMuteFirmata() {
		sysex(MUTE_FIRMATA, new byte[] { 0 });
	}

	private void queryVersion() {
		write(REPORT_VERSION);
	}

	private void clearAllBuffers() {
		bluetoothBuffer.clear();
		uartBuffer.clear();
	}

	public BluetoothService getBTService() {
		return bluetoothService;
	}

	private void onClose(boolean isManually) {
		for (ArduinoFirmataEventHandler eventHandler : eventHandlers) {
			if (eventHandler != null)
				eventHandler.onClose(isManually);
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
				bluetoothBuffer.add(bytes[i]);
				// processInput(bytes[i]);
			}

		}

		@Override
		public void onStateChanged(final int state, final boolean isManually) {
			// TODO Auto-generated method stub
			uiThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					if (state == BluetoothService.STATE_NONE) {
						onClose(isManually);
					}
				}
			});
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
		stopBuffersThreads();
		clearAllBuffers();
		resetProcessInput();
		isVersionQueried=false;
		// if (bluetoothBufferListeningThread == null ||
		// bluetoothBufferListeningThread.isInterrupted())
		bluetoothBufferListeningThread = new BluetoothBufferListeningThread();
		uartListeningThread = new UartListeningThread();
		// else {
		// uartListeningThread.isRunning = true;
		// uartListeningThread.start();
		// }
		enableReporting();
		setAllPinsAsInput();
		initUart();
		queryVersion();
		uiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				onConnect();
				String mConnectedDeviceName = device.getName();
				Toast.makeText(context, "Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private byte readByteFromUartBuffer() throws InterruptedException {
		return uartBuffer.take().byteValue();
	}

	private byte readByteFromBluetoothBuffer() throws InterruptedException {
		return bluetoothBuffer.take().byteValue();
	}

	public void clearAllHandlers() {
		eventHandlers.clear();
		dataHandlers.clear();
		frameHandlers.clear();
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

	public static enum BaudRate {
		_1200((byte) 0x00), _2400((byte) 0x01), _4800((byte) 0x02), _9600(
				(byte) 0x03), _14400((byte) 0x04), _19200((byte) 0x05), _28800(
				(byte) 0x06), _38400((byte) 0x07), _57600((byte) 0x08), _115200(
				(byte) 0x09);

		byte value;

		BaudRate(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

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

					arduinoLibraryVersion = readByteFromUartBuffer();
					byte shieldId = readByteFromUartBuffer();
					boolean found = false;
					for (UIShield shield : UIShield.values()) {
						if (shieldId == shield.getId())
							found = true;
					}
					if (!found) {
						uartBuffer.clear();
						continue;
					}
					byte instanceId = readByteFromUartBuffer();
					byte functionId = readByteFromUartBuffer();
					ShieldFrame frame = new ShieldFrame(shieldId, instanceId,
							functionId);
					int argumentsNumber = readByteFromUartBuffer();
					for (int i = 0; i < argumentsNumber; i++) {
						int length = readByteFromUartBuffer() & 0xff;
						if (length <= 0) {
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
						uartBuffer.clear();
						continue;
					}

					for (ArduinoFirmataShieldFrameHandler frameHandler : frameHandlers) {
						frameHandler.onNewShieldFrameReceived(frame);
					}
				} catch (InterruptedException e) {
					return;
				}
			}
		}
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
			while (!this.isInterrupted()) {
				if (!isBootloader)
					try {
						processInput(readByteFromBluetoothBuffer());
					} catch (InterruptedException e) {
						return;
					}

			}
		}
	}

}