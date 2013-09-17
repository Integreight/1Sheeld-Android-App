package com.integreight.onesheeld.services;



import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.integreight.firmatabluetooth.ArduinoFirmata;
import com.integreight.firmatabluetooth.ArduinoFirmataEventHandler;
import com.integreight.firmatabluetooth.BluetoothService;
import com.integreight.onesheeld.R;

public class OneSheeldService extends Service {

//    private static final String TAG = "OneSheeldService";
//    private static final boolean D = true;
    
	private ArduinoFirmata arduinoFirmata;
	private final IBinder mBinder = new OneSheeldBinder();
	private BluetoothAdapter mBluetoothAdapter = null;
	private ArduinoFirmataEventHandler arduinoEventHandler= new ArduinoFirmataEventHandler() {
		
		@Override
		public void onError(String errorMessage) {
			// TODO Auto-generated method stub
			sheeldConnectedMessageToActivity(COMMUNICAITON_ERROR);
			
		}
		
		@Override
		public void onConnect() {
			// TODO Auto-generated method stub
			sheeldConnectedMessageToActivity(SHEELD_BLUETOOTH_CONNECTED);
			showNotification();

		}
		
		@Override
		public void onClose() {
			// TODO Auto-generated method stub
			sheeldConnectedMessageToActivity(SHEELD_CLOSE_CONNECTION);
			stopSelf();
			
		}
	};
	
	public static final String SHEELD_BLUETOOTH_CONNECTED = "com.integreight.SHEELD_BLUETOOTH_CONNECTED";
    public static final String COMMUNICAITON_ERROR = "com.integreight.COMMUNICAITON_ERROR";
    public static final String SHEELD_CLOSE_CONNECTION = "com.integreight.SHEELD_CLOES_CONNECTION";
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		arduinoFirmata = new ArduinoFirmata(this);
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		
		String address = intent.getExtras().getString(BluetoothService.EXTRA_DEVICE_ADDRESS);
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        
        arduinoFirmata.addEventHandler(arduinoEventHandler);
        arduinoFirmata.connect(device);
		return START_REDELIVER_INTENT;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		while(!arduinoFirmata.close());
		hideNotifcation();
		
		super.onDestroy();
	}

	private void showNotification(){
		Notification.Builder build=new Notification.Builder(this);
		build.setSmallIcon(R.drawable.ic_launcher);
		build.setContentText("The service is running!");
		build.setContentTitle("1Sheeld is connected");
		build.setTicker("1Sheeld is connected!");
//		build.setContentInfo("");
		build.setWhen(System.currentTimeMillis());
//		PendingIntent pendingIntent = PendingIntent.getService(this, 0, new Intent(this, OneSheeldService.class), 0);
		
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );
//		build.addAction(R.drawable.action_cancel, "Close Service",pendingIntent );

		
		//build.setContentIntent(pendingIntent);
		Notification notification=build.build();
		startForeground(1, notification);
	}
	
	private void hideNotifcation(){
		stopForeground(true);
	}
	
	private void sheeldConnectedMessageToActivity(String event) {
	    Intent intent = new Intent(event);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	public ArduinoFirmata getFirmata(){
		 return arduinoFirmata;
	}

	 public class OneSheeldBinder extends Binder {
		 public OneSheeldService getService() {
	            // Return this instance of LocalService so clients can call public methods
	            return OneSheeldService.this;
	        }
		
	    }
	
}
