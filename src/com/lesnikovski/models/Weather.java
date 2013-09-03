package com.lesnikovski.models;

public class Weather {
	private String date;
	private float precipMM;
	private int tempMaxC;
	private int tempMaxF;
	private int tempMinC;
	private int tempMinF;
	private int weatherCode;
	private String winddir16Point;
	private int winddirDegree;
	private String winddirection;
	private int windspeedKmph;
	private int windspeedMiles;
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public float getPrecipMM() {
		return precipMM;
	}
	public void setPrecipMM(float precipMM) {
		this.precipMM = precipMM;
	}
	public int getTempMaxC() {
		return tempMaxC;
	}
	public void setTempMaxC(int tempMaxC) {
		this.tempMaxC = tempMaxC;
	}
	public int getTempMaxF() {
		return tempMaxF;
	}
	public void setTempMaxF(int tempMaxF) {
		this.tempMaxF = tempMaxF;
	}
	public int getTempMinC() {
		return tempMinC;
	}
	public void setTempMinC(int tempMinC) {
		this.tempMinC = tempMinC;
	}
	public int getTempMinF() {
		return tempMinF;
	}
	public void setTempMinF(int tempMinF) {
		this.tempMinF = tempMinF;
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
	public String getWinddirection() {
		return winddirection;
	}
	public void setWinddirection(String winddirection) {
		this.winddirection = winddirection;
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
}
