package com.fewpeople.mitten.contracts;

public interface WebApiContract<T> {
	public T get(double lat, double lon);
}
