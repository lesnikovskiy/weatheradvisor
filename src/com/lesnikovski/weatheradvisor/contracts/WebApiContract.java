package com.lesnikovski.weatheradvisor.contracts;

public interface WebApiContract<T> {
	public T get(double lat, double lon);
}
