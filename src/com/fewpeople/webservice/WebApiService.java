package com.fewpeople.webservice;

import android.text.TextUtils;

import com.fewpeople.mitten.contracts.WebApiContract;
import com.fewpeople.models.WeatherData;
import com.fewpeople.utils.HttpUtil;
import com.google.gson.Gson;

public class WebApiService implements WebApiContract<WeatherData> {	
	// DOCS: http://developer.worldweatheronline.com/documentation
	static final private String URL = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=%s,%s&format=json&num_of_days=1&key=z4bqactn5v7gu6ttdz6agtkd"; 
	// without forecast
	//static final private String URL_FX = "http://api.worldweatheronline.com/free/v1/weather.ashx?q=%s,%s&format=json&num_of_days=1&key=z4bqactn5v7gu6ttdz6agtkd";
	
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
