package io.wonderfuel.fueldb.api.utils;

/**
 * @author Joris Basiglio
 *
 */
public class Wrapper<T> {

	private T value;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	
}
