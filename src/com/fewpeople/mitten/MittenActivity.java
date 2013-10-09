package com.fewpeople.mitten;

import static com.fewpeople.constants.IntentConstants.COLD_STATE;
import static com.fewpeople.constants.IntentConstants.DEWPOINT;
import static com.fewpeople.constants.IntentConstants.HUMIDEX;
import static com.fewpeople.constants.IntentConstants.HUMIDITY;
import static com.fewpeople.constants.IntentConstants.PRESSURE;
import static com.fewpeople.constants.IntentConstants.PRESSUREDIFF;
import static com.fewpeople.constants.IntentConstants.STOP_SERVICE;
import static com.fewpeople.constants.IntentConstants.TEMP;
import static com.fewpeople.constants.IntentConstants.TEMPDIFF;
import static com.fewpeople.constants.IntentConstants.TEMP_STATE;
import static com.fewpeople.constants.IntentConstants.TITLE;
import static com.fewpeople.constants.IntentConstants.WARMER_STATE;
import static com.fewpeople.constants.IntentConstants.WARNING;
import static com.fewpeople.constants.IntentConstants.WINDCHILL;
import static com.fewpeople.constants.IntentConstants.WINDDIFF;
import static com.fewpeople.constants.IntentConstants.WINDSPEED;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fewpeople.services.MittenService;
import com.fewpeople.mitten.R;

public class MittenActivity extends Activity  {		
	private static final String TAG = "MittenActivity";
	
	private Intent intent;
	private TextView weatherTextView;
	private TextView warningTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_advisor);	
				
		intent = new Intent(this, MittenService.class);		
		startService();	
		
		Intent currentIntent = getIntent();
		if (currentIntent != null && currentIntent.getExtras() != null) {
			updateUi(currentIntent);
		}
	}	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (!isServiceRunning())
			startService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.weather_advisor, menu);
		
		return true;
	}
	
	private BroadcastReceiver broadcaseReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateUi(intent);
		}
	};
	
	private void updateUi(Intent intent) {
		String title = intent.getStringExtra(TITLE);
		String temp = intent.getStringExtra(TEMP);
		String perceived_temp = intent.getStringExtra(HUMIDEX);
		String dewpoint = intent.getStringExtra(DEWPOINT);
		String humidity = intent.getStringExtra(HUMIDITY);
		String pressure = intent.getStringExtra(PRESSURE);
		String windSpeed = intent.getStringExtra(WINDSPEED);
		String tempDiff = intent.getStringExtra(TEMPDIFF);
		String pressDiff = intent.getStringExtra(PRESSUREDIFF);
		String windDiff = intent.getStringExtra(WINDDIFF);
		String windChill = intent.getStringExtra(WINDCHILL);
		String background = intent.getStringExtra(TEMP_STATE);
		String warning = intent.getStringExtra(WARNING);
		
		boolean stopService = intent.getBooleanExtra(STOP_SERVICE, false);
		
		weatherTextView = (TextView) findViewById(R.id.weatherTextView);
		warningTextView = (TextView) findViewById(R.id.warningTextView);
		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
		
		weatherTextView.setText(String.format("Observation time: %s\nTemperature: %s\u2103\nHumidex: %s\u2103\nDew Point: %s\u2103\nHumidity: %s%%\nPressure: %s mm\nWindspeed: %s Kmph\nWind Chill: %s\u2103\n\n%s\n%s\n%s\n", 
				title, temp, perceived_temp, dewpoint, humidity, pressure, windSpeed, windChill, tempDiff, pressDiff, windDiff));
		
		if (!TextUtils.isEmpty(warning))
			warningTextView.setText(warning);
		if (background != null) {
			if (background.equals(WARMER_STATE))
				rootLayout.setBackgroundColor(Color.YELLOW);
			else if (background.equals(COLD_STATE))
				rootLayout.setBackgroundColor(Color.CYAN);
			else
				rootLayout.setBackgroundColor(Color.WHITE);
		}
		
		if (stopService)
			stopService();
	}
	
	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (MittenService.class.getName().equals(service.service.getClassName())) 
				return true;
		}
		
		return false;
	}
	
	private void startService() {
		try {
			startService(intent);
			registerReceiver(broadcaseReceiver, new IntentFilter(MittenService.BROADCAST_ACTION));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void stopService() {
		try {
			stopService(intent);
			unregisterReceiver(broadcaseReceiver);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
