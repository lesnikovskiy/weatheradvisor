package com.lesnikovski.webservice;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.lesnikovski.models.WeatherData;
import com.lesnikovski.utils.HttpUtil;
import com.lesnikovski.weatheradvisor.contracts.WebApiContract;

public class WebApiService implements WebApiContract<WeatherData> {	
	static final private String URL = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=%s,%s&format=json&num_of_days=1&key=z4bqactn5v7gu6ttdz6agtkd"; 
	
	public WeatherData get(double lat, double lon) {
		WeatherData data = new WeatherData();
				
		String json = HttpUtil.get(String.format(URL, lat, lon));	
		if (TextUtils.isEmpty(json))
			return null;
		
		Gson gson = new Gson();
		data = gson.fromJson(json, WeatherData.class);		
		
		return data;
	}
}
