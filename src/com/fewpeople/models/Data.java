package com.fewpeople.models;

import java.util.List;

public class Data {
	private List<Condition> current_condition;
	private List<Weather> weather;

	public List<Condition> getCurrent_condition() {
		return current_condition;
	}

	public void setCurrent_condition(List<Condition> current_condition) {
		this.current_condition = current_condition;
	}

	public List<Weather> getWeather() {
		return weather;
	}

	public void setWeather(List<Weather> weather) {
		this.weather = weather;
	}
}
