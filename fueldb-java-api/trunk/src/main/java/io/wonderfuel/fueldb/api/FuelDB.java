package io.wonderfuel.fueldb.api;

import io.wonderfuel.fueldb.api.listener.DataListener;

import org.json.simple.JSONObject;

public interface FuelDB {

	public abstract void connect();

	public abstract void subscribe(String point, DataListener listener);

	public abstract void unsubscribe(String point);

	public abstract void read(String point, DataListener listener);

	public abstract JSONObject readSync(String point);

	public abstract void browse(String point, DataListener listener);

	public abstract JSONObject browseSync(String point);

	public abstract void write(String point, JSONObject value);

	public abstract void remove(String point);

	public abstract void setOnClose(DataListener onClose);

	public abstract void setOnOpen(DataListener onOpen);

}