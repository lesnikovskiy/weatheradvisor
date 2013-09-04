package com.lesnikovski.models;


public class LocalData {
	private long id;
	private String observationTime;
	private int tempC;
	private int visibility;
	private int cloudcover;
	private int humidity;
	private int pressure;
	private int windspeedKmph;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getObservationTime() {
		return observationTime;
	}
	public void setObservationTime(String observationTime) {
		this.observationTime = observationTime;
	}
	public int getTempC() {
		return tempC;
	}
	public void setTempC(int tempC) {
		this.tempC = tempC;
	}
	public int getVisibility() {
		return visibility;
	}
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
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
	public int getPressure() {
		return pressure;
	}
	public void setPressure(int pressure) {
		this.pressure = pressure;
	}
	public int getWindspeedKmph() {
		return windspeedKmph;
	}
	public void setWindspeedKmph(int windspeedKmph) {
		this.windspeedKmph = windspeedKmph;
	}
}
