package io.wonderfuel.fueldb.api.listener;

import org.json.simple.JSONObject;

public interface DataListener {

	public void handle(JSONObject data);
	
}
