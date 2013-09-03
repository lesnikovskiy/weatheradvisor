package com.lesnikovski.weatheradvisor;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.lesnikovski.models.Data;
import com.lesnikovski.models.WeatherData;
import com.lesnikovski.weatheradvisor.contracts.WebApiContract;
import com.lesnikovski.webservice.WebApiService;

public class WeatherAdvisorActivity extends Activity {
	// web service link http://api.worldweatheronline.com/free/v1/weather.ashx?q=50%2C30&format=json&num_of_days=1&includelocation=yes&key=z4bqactn5v7gu6ttdz6agtkd
	private WebApiContract<WeatherData> webApi;
	private Button getWeatherButton;
	private WeatherData weatherData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather_advisor);
		
		getWeatherButton = (Button) findViewById(R.id.getWeatherData);
		getWeatherButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {						
						webApi = new WebApiService();
						weatherData = webApi.get();
					}
				});	
				
				thread.start();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.weather_advisor, menu);
		
		return true;
	}
}
