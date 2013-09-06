package com.lesnikovski.weatheradvisor;

import static com.lesnikovski.constants.IntentConstants.COLD_STATE;
import static com.lesnikovski.constants.IntentConstants.HUMIDITY;
import static com.lesnikovski.constants.IntentConstants.PRESSURE;
import static com.lesnikovski.constants.IntentConstants.PRESSUREDIFF;
import static com.lesnikovski.constants.IntentConstants.TEMP;
import static com.lesnikovski.constants.IntentConstants.TEMPDIFF;
import static com.lesnikovski.constants.IntentConstants.TEMP_STATE;
import static com.lesnikovski.constants.IntentConstants.TITLE;
import static com.lesnikovski.constants.IntentConstants.WARMER_STATE;
import static com.lesnikovski.constants.IntentConstants.WINDDIFF;
import static com.lesnikovski.constants.IntentConstants.WINDSPEED;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lesnikovski.services.WeatherAdvisorService;

public class WeatherAdvisorActivity extends Activity  {		
	private static final String TAG = "WeatherAdvisorActivity";
	
	private Intent intent;
	private TextView weatherTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_advisor);	
		
		intent = new Intent(this, WeatherAdvisorService.class);		
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
		String humidity = intent.getStringExtra(HUMIDITY);
		String pressure = intent.getStringExtra(PRESSURE);
		String windSpeed = intent.getStringExtra(WINDSPEED);
		String tempDiff = intent.getStringExtra(TEMPDIFF);
		String pressDiff = intent.getStringExtra(PRESSUREDIFF);
		String windDiff = intent.getStringExtra(WINDDIFF);
		String background = intent.getStringExtra(TEMP_STATE);
		
		weatherTextView = (TextView) findViewById(R.id.weatherTextView);
		RelativeLayout rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
		
		weatherTextView.setText(String.format("Observation time: %s\nTemperature: %s\u2103\nHumidity: %s%%\nPressure: %s mm\nWindspeed: %s Kmph\n\n%s\n%s\n%s\n", 
				title, temp, humidity, pressure, windSpeed, tempDiff, pressDiff, windDiff));
		
		if (background.equals(WARMER_STATE))
			rootLayout.setBackgroundColor(Color.YELLOW);
		else if (background.equals(COLD_STATE))
			rootLayout.setBackgroundColor(Color.CYAN);
		else
			rootLayout.setBackgroundColor(Color.WHITE);
	}
	
	private boolean isServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (WeatherAdvisorService.class.getName().equals(service.service.getClassName())) 
				return true;
		}
		
		return false;
	}
	
	private void startService() {
		try {
			startService(intent);
			registerReceiver(broadcaseReceiver, new IntentFilter(WeatherAdvisorService.BROADCAST_ACTION));
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
