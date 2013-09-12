package com.integreight.onesheeld.activities;


import java.util.Arrays;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.integreight.onesheeld.R;
import com.integreight.onesheeld.UIShield;
import com.integreight.onesheeld.adapters.ShieldsListAdapter;

public class MainActivity extends SherlockActivity {

	List<UIShield>  shieldsUIList;
    ShieldsListAdapter adapter ;
    ListView shieldsListView ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		shieldsUIList =Arrays.asList( UIShield.values());
		shieldsListView = (ListView) findViewById(R.id.main_activity_shields_listview);
		adapter = new ShieldsListAdapter(this, shieldsUIList);
		shieldsListView.setAdapter(adapter);
		shieldsListView.setCacheColorHint(Color.TRANSPARENT);
		shieldsListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);    
		shieldsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lv, View item, int position, long id) {
 
                ListView lView = (ListView) lv;
 
//                SimpleAdapter adapter = (SimpleAdapter) lView.getAdapter();
 
 
                /** The clicked Item in the ListView */
                RelativeLayout rLayout = (RelativeLayout) item;
 
                /** Getting the toggle button corresponding to the clicked item */
                ToggleButton tgl = (ToggleButton) rLayout.getChildAt(0);
                ImageView image=(ImageView) rLayout.getChildAt(1);
 
//                String strStatus = "";
                if(tgl.isChecked()){
                    tgl.setChecked(false);
                    tgl.setVisibility(View.INVISIBLE);
                    image.setVisibility(View.INVISIBLE);
                   
//                    status[position]=false;
                }else{
                    tgl.setChecked(true);
                    tgl.setVisibility(View.VISIBLE);
                    image.setVisibility(View.VISIBLE);

//                    strStatus = "On";
                   
                }
//                Toast.makeText(getBaseContext(), (String) hm.get("txt") + " : " + strStatus, Toast.LENGTH_SHORT).show();
            }

		
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
