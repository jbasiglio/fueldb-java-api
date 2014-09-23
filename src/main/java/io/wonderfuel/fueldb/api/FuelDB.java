package io.wonderfuel.fueldb.api;

import io.wonderfuel.fueldb.api.listener.DataListener;

import java.io.IOException;

import javax.websocket.DeploymentException;

import org.json.simple.JSONObject;

public interface FuelDB {

	public abstract void connect() throws DeploymentException, IOException;

	public abstract void subscribe(String point, DataListener listener) throws IOException;

	public abstract void unsubscribe(String point) throws IOException;

	public abstract void read(String point, DataListener listener) throws IOException;

	public abstract JSONObject readSync(String point) throws IOException, InterruptedException;

	public abstract void browse(String point, DataListener listener) throws IOException;

	public abstract JSONObject browseSync(String point) throws IOException, InterruptedException;

	public abstract void write(String point, JSONObject value) throws IOException;

	public abstract void remove(String point) throws IOException;

	public abstract void setOnClose(DataListener onClose);

	public abstract void setOnOpen(DataListener onOpen);

}