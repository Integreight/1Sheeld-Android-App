package com.integreight.firmatabluetooth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.integreight.firmatabluetooth.BluetoothService.BluetoothServiceHandler;

public class Jodem {

	public static final byte SOH = 0x01;
	public static final byte STX = 0x02;
	public static final byte EOT = 0x04;
	public static final byte ACK = 0x06;
	public static final byte DLE = 0x10;
	public static final byte NAK = 0x15;
	public static final byte CAN = 0x18;
	public static final byte CRC = 0x43; //C
	public static final byte SUB = 0x1A;
	BluetoothService btService;
	int timeoutCounter=3;
	Thread timeoutThread;
	Runnable timeoutThreadRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
			do{
			
				Thread.sleep(1000);
				Log.d("bootloader","Time left for timeout: "+timeoutCounter+" Seconds!");
			timeoutCounter--;
			}while(timeoutCounter>=0);
			jodemHandler.onTimout();
			Log.d("bootloader","Timeout Occured!");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
		}
	};

	LinkedBlockingQueue<Byte> buffer = new LinkedBlockingQueue<Byte>();
	
	public interface JodemEventHandler {
		public void onError(String error);
		public void onProgress(int totalBytes,int sendBytes, int errorCount);
		public void onSuccess();
		public void onTimout();
		
	}
	
	private JodemEventHandler jodemHandler;

	private int crctable[] = {
			0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
			0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
			0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
			0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
			0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
			0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
			0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
			0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
			0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
			0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
			0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
			0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
			0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
			0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
			0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
			0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
			0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
			0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
			0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
			0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
			0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
			0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
			0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
			0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
			0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
			0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
			0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
			0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
			0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
			0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
			0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
			0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
	};


	public Jodem(BluetoothService btService, JodemEventHandler jodemHandler){
		this.btService=btService;
		this.btService.addBluetoothServiceHandler(btHandler);
		this.jodemHandler=jodemHandler;
	}
	
	public void abort(int count){

		for (int counter=0;counter<count;counter++){
			write(CAN);
		}
	}

	public boolean send(InputStream inputStream, int retry) throws InterruptedException{
		buffer.clear();
		byte[] fileBuffer=new byte[10240];
		int fileLength;
		try {
			fileLength = inputStream.read(fileBuffer);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		ByteArrayInputStream stream=new ByteArrayInputStream(fileBuffer,0,fileLength);
		timeoutThread=new Thread(timeoutThreadRunnable);
		timeoutThread.start();
		while(readByteFromBuffer()!=NAK);
		write(ACK);
		int packet_size = 128;


		int error_count = 0;
		int crc_mode = 0;
		int cancel = 0;
		int crc;
		while(true){
			byte readChar = readByteFromBuffer(); ////READ CHAR

			if (readChar == NAK){
				crc_mode = 0;
				break;
			}
			else if (readChar == CRC){
				crc_mode = 1;
				break;
			}
			else if (readChar == CAN){

					Log.d("bootloader","received CAN");
					
				if (cancel!=0)
					return false;
				else
					cancel = 1;
			}
			else{
				String s="send ERROR expected NAK/CRC, got "+readChar;
				Log.d("bootloader",s);
				jodemHandler.onError(s);
				if(timeoutThread.isAlive())timeoutThread.interrupt();
			}

			error_count += 1;
			if (error_count >= retry){
				abort(2);
				return false;
			}
		}


		error_count = 0;
		int success_count = 0;
		int total_packets = 0;
		byte sequence = 1;
		while (true){
			byte[] data=new byte[packet_size];
			int readCount=0;
			readCount = stream.read(data,0,packet_size);
			if (readCount<=0){
				Log.d("bootloader","sending EOT");
				break;
			}
			total_packets += 1;
			for(int i=readCount;i<packet_size;i++){
				data[i]=SUB;
			}
			if (crc_mode!=0)
				crc = calculateCrc(data);
			else
				crc = calculateChecksum(data);

			//  // emit packet
			buffer.clear();
			while(true){
				if (packet_size == 128)
					write(SOH);
				else  //// packet_size == 1024
					write(STX);

				write(sequence);
				write((byte)(0xff - sequence));
				write(data);
				if (crc_mode==1){
					write((byte)(crc >> 8));
					write((byte)(crc & 0xff));
				}
				else
					write((byte)crc);

				byte readChar = readByteFromBuffer();
				if (readChar == ACK){
					success_count += 1;
//					if (callable(callback))
//						callback(total_packets, success_count, error_count);
					jodemHandler.onProgress(fileLength, (readCount>=128)?success_count*readCount:((success_count-1)*packet_size+readCount), error_count);
					break;
				}
				if (readChar == NAK){
					error_count += 1;
					jodemHandler.onProgress(fileLength, (readCount>=128)?success_count*readCount:((success_count-1)*packet_size+readCount), error_count);
//					if (callable(callback))
//						callback(total_packets, success_count, error_count)
						if (error_count >= retry){
							//// excessive amounts of retransmissions requested,
							//// abort transfer
							abort(2);
							String s="excessive NAKs, transfer aborted";
							Log.d("bootloader",s);
							jodemHandler.onError(s);
							if(timeoutThread.isAlive())timeoutThread.interrupt();
							return false;
						}
					// return to loop and resend
					continue;
				}
				//  // protocol error
				abort(2);
				String s="protocol error";
				Log.d("bootloader",s);
				jodemHandler.onError(s);
				if(timeoutThread.isAlive())timeoutThread.interrupt();
				return false;
			}
			// // keep track of sequence
			sequence = (byte)((sequence + 1) % 0x100);
		}

		while (true){
			// end of transmission
			write(EOT);

			//An ACK should be returned
			byte readChar = readByteFromBuffer();
			if (readChar == ACK){
				if(timeoutThread.isAlive())timeoutThread.interrupt();
				jodemHandler.onSuccess();
				break;
				}
			else{
				error_count += 1;
				if (error_count >= retry){
					abort(2);
					String s="EOT was not ACKd, transfer aborted";
					Log.d("bootloader",s);
					jodemHandler.onError(s);
					if(timeoutThread.isAlive())timeoutThread.interrupt();
					return false;
				}
			}
		}
		return true;
	}
	
	public byte readByteFromBuffer() throws InterruptedException{
		timeoutCounter=3;
		return buffer.take().byteValue();
	}
	
	public int calculateChecksum(byte[] data){
		int checksum=0;
		for(int i=0;i<data.length;i++){
			checksum+=data[i];
		}
		return checksum % 256;
	}

	public int calculateCrc(byte[] data){
		int crc=0;
		for (byte readChar:data)
			crc = (crc << 8) ^ crctable[((crc >> 8) ^ readChar) & 0xff];
		return crc & 0xffff;
	}
	
    public void write(byte[] writeData){

    	btService.write(writeData);

    }

    public void write(byte writeData){
    	btService.write(writeData);
    }
	
    BluetoothServiceHandler btHandler=new BluetoothServiceHandler() {
		
		@Override
		public void onDataReceived(byte[] bytes, int length) {
			// TODO Auto-generated method stub
			for(int i = 0; i < length; i++){
				buffer.add(bytes[i]);
            }
			
		}

		@Override
		public void onStateChanged(final int state, final boolean isManually) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onDataWritten(byte[] bytes) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onConnected(final BluetoothDevice device) {
			
		}

		@Override
		public void onError(final String error) {
			// TODO Auto-generated method stub
			
		}
	};
	

}
