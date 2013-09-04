package com.lesnikovski.weatheradvisor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
	}
	
	public void startServiceClick(View view) {
		try {
			startService(intent);
			registerReceiver(broadcaseReceiver, new IntentFilter(WeatherAdvisorService.BROADCAST_ACTION));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void stopServiceClick(View view) {
		try {
			stopService(intent);
			unregisterReceiver(broadcaseReceiver);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
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
		String title = intent.getStringExtra("title");
		int temp = intent.getIntExtra("temp", -1);
		int humidity = intent.getIntExtra("humidity", -1);
		int pressure = intent.getIntExtra("pressure", -1);
		int windSpeed = intent.getIntExtra("windSpeed", -1);
		String diff = intent.getStringExtra("diff");
		
		weatherTextView = (TextView) findViewById(R.id.weatherTextView);
		
		weatherTextView.setText(String.format("Observation time: %s\nTemperature: %s\u2103\nHumidity: %s%%\nPressure: %s mm\nWindspeed: %s Kmph\n\n%s\n", 
				title, temp, humidity, pressure, windSpeed, diff));
	}
}
