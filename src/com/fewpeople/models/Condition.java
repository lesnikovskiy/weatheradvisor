package com.fewpeople.models;

import java.util.List;

public class Condition {
	private int cloudcover;
	private int humidity;
	private String observation_time;
	private float precipMM;
	private int pressure;
	private int temp_C;
	private int visibility;
	private int weatherCode;
	private String winddir16Point;
	private int winddirDegree;
	private int windspeedKmph;
	private int windspeedMiles;
	private List<WeatherDesc> weatherDesc;
	
	public int getCloudcover() {
		return cloudcover;
	}
	public void setCloudcover(int cloudcover) {
		this.cloudcover = cloudcover;
	}
	public int getHumidity() {
		return humidity;
	}
	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}
	public String getObservation_time() {
		return observation_time;
	}
	public void setObservation_time(String observation_time) {
		this.observation_time = observation_time;
	}
	public float getPrecipMM() {
		return precipMM;
	}
	public void setPrecipMM(float precipMM) {
		this.precipMM = precipMM;
	}
	public int getPressure() {
		return pressure;
	}
	public void setPressure(int pressure) {
		this.pressure = pressure;
	}
	public int getTemp_C() {
		return temp_C;
	}
	public void setTemp_C(int temp_C) {
		this.temp_C = temp_C;
	}
	public int getVisibility() {
		return visibility;
	}
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	public int getWeatherCode() {
		return weatherCode;
	}
	public void setWeatherCode(int weatherCode) {
		this.weatherCode = weatherCode;
	}
	public String getWinddir16Point() {
		return winddir16Point;
	}
	public void setWinddir16Point(String winddir16Point) {
		this.winddir16Point = winddir16Point;
	}
	public int getWinddirDegree() {
		return winddirDegree;
	}
	public void setWinddirDegree(int winddirDegree) {
		this.winddirDegree = winddirDegree;
	}
	public int getWindspeedKmph() {
		return windspeedKmph;
	}
	public void setWindspeedKmph(int windspeedKmph) {
		this.windspeedKmph = windspeedKmph;
	}
	public int getWindspeedMiles() {
		return windspeedMiles;
	}
	public void setWindspeedMiles(int windspeedMiles) {
		this.windspeedMiles = windspeedMiles;
	}
	public List<WeatherDesc> getWeatherDesc() {
		return weatherDesc;
	}
	public void setWeatherDesc(List<WeatherDesc> weatherDesc) {
		this.weatherDesc = weatherDesc;
	}
}
