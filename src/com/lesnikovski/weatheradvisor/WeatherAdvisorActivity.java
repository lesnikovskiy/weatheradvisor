package com.lesnikovski.weatheradvisor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.lesnikovski.services.WeatherAdvisorService;

public class WeatherAdvisorActivity extends Activity  {		
	private Intent intent;
	private TextView weatherTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_advisor);	
		
		intent = new Intent(this, WeatherAdvisorService.class);		
	}
	
	public void startServiceClick(View view) {
		startService(intent);
		registerReceiver(broadcaseReceiver, new IntentFilter(WeatherAdvisorService.BROADCAST_ACTION));
	}
	
	public void stopServiceClick(View view) {
		stopService(intent);
		unregisterReceiver(broadcaseReceiver);
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
			String title = intent.getStringExtra("title");
			String temp = intent.getStringExtra("temp");
			String humidity = intent.getStringExtra("humidity");
			String pressure = intent.getStringExtra("pressure");
			String windSpeed = intent.getStringExtra("windSpeed");
			
			weatherTextView = (TextView) findViewById(R.id.weatherTextView);
			
			weatherTextView.setText(String.format("%s\n%s\n%s\n%s\n%s\n", title, temp, humidity, pressure, windSpeed));
		}
	};
}
