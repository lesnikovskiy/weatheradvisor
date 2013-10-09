package com.fewpeople.utils;

public class WeatherUtil {
	public static int getHumidex(int temperature, int humidity) {
		double t = (double) temperature;
		double h = (double) humidity;
		double e1 = (6.112*Math.pow(10, (7.5*t/(237.7+t))*h/100));
		double humidex = t + (0.5555*(e1-10));
		
		return (int) Math.round(humidex);
	}
	
	public static int getDewPoint(int temperature, int humidity) {
		double t = (double) temperature;
		double h = (double) humidity;
		
		double intermediateValue = (Math.log(h / 100) + ((17.27 * t) / (237.3 + t))) / 17.27;
		double dewpoint = (237.3 * intermediateValue) / (1 - intermediateValue);
		
		return (int) Math.round(dewpoint);
	}
	
	public static int getWindChill(int temperature, int windSpeed) {
		double t = (double) temperature;
		double w = (double) windSpeed;
		
		double windChillTemp = 0.045*(5.2735*Math.sqrt(w) + 10.45 - 0.2778*w)*(t - 33.0)+33;
		
		return (int) Math.round(windChillTemp);
	}
	
	public static int getWindChillFactor(int temperature, int windSpeed) {
		double t = (double) temperature;
		double w = (double) windSpeed;
		
		double windChillFactor = 1.1626*(5.2735*Math.sqrt(w) + 10.45 - 0.2778*w)*(33.0 - t);
		
		return (int) Math.round(windChillFactor);
	}
}
