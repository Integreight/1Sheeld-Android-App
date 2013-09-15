package com.integreight.onesheeld.activities;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.UIShield;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;
import com.integreight.onesheeld.services.OneSheeldService;

public class MainActivity extends SherlockActivity {

	List<UIShield> shieldsUIList;
	ShieldsListAdapter adapter;
	ListView shieldsListView;
	
	private static final String TAG = "MainActivity";
    private static final boolean D = true;
	
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    
    private BluetoothAdapter mBluetoothAdapter = null;
    
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(OneSheeldService.COMMUNICAITON_ERROR)){
            	
            }
            else if(action.equals(OneSheeldService.SHEELD_BLUETOOTH_CONNECTED)){
            	Log.e(TAG, "- ARDUINO CONNECTED -");
//            	Intent buttonsActivityIntent=new Intent(MainActivity.this,ButtonsActivity.class);
//            	startActivity(buttonsActivityIntent);
            }
            else if(action.equals(OneSheeldService.SHEELD_CLOSE_CONNECTION)){
            	
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		shieldsUIList = Arrays.asList(UIShield.values());
		shieldsListView = (ListView) findViewById(R.id.main_activity_shields_listview);
		adapter = new ShieldsListAdapter(this, shieldsUIList);
		shieldsListView.setAdapter(adapter);
		shieldsListView.setCacheColorHint(Color.TRANSPARENT);
		shieldsListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		shieldsListView.setDrawingCacheEnabled(true);
		shieldsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> lv, View item, int position,
					long id) {
				RelativeLayout rLayout = (RelativeLayout) item;
				ToggleButton selectionMark = (ToggleButton) rLayout
						.getChildAt(0);
				ImageView selectionCircle = (ImageView) rLayout.getChildAt(1);

				if (selectionMark.isChecked()) {
					selectionMark.setChecked(false);
					selectionMark.setVisibility(View.INVISIBLE);
					selectionCircle.setVisibility(View.INVISIBLE);
					UIShield.getItem(position + 1).setToggleStatus(false);
				} else {
					selectionMark.setChecked(true);
					selectionMark.setVisibility(View.VISIBLE);
					selectionCircle.setVisibility(View.VISIBLE);
					UIShield.getItem(position + 1).setToggleStatus(true);

				}
			}

		});
		
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(OneSheeldService.COMMUNICAITON_ERROR));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(OneSheeldService.SHEELD_BLUETOOTH_CONNECTED));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(OneSheeldService.SHEELD_CLOSE_CONNECTION));
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }
	}

	   public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if(D) Log.d(TAG, "onActivityResult " + resultCode);
	        switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE:
	            // When DeviceListActivity returns with a device to connect
	            if (resultCode == Activity.RESULT_OK) {
	                connectDevice(data, true);
	            }
	            break;
	        case REQUEST_ENABLE_BT:
	            // When the request to enable Bluetooth returns
	            if (resultCode != Activity.RESULT_OK) {
	                Log.d(TAG, "BT not enabled");
	                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
	                finish();
	            }
	        }
	    }
	   
	    private void connectDevice(Intent data, boolean secure) {
	        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
	        Intent intent = new Intent(this, OneSheeldService.class);
	        intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
	        startService(intent);

	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.main_activity_action_search:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            return true;
        }
        return false;
    }


}
