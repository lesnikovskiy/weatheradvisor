package com.lesnikovski.weatheradvisor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.lesnikovski.models.WeatherData;
import com.lesnikovski.services.WeatherAdvisorService;
import com.lesnikovski.weatheradvisor.contracts.WebApiContract;
import com.lesnikovski.webservice.WebApiService;

public class WeatherAdvisorActivity extends Activity  {		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_advisor);		
	}
	
	public void startServiceClick(View view) {
		startService(new Intent(this, WeatherAdvisorService.class));
	}
	
	public void stopServiceClick(View view) {
		stopService(new Intent(this, WeatherAdvisorService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather_advisor, menu);
		
		return true;
	}
}
