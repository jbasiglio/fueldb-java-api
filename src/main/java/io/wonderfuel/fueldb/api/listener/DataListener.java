package io.wonderfuel.fueldb.api.listener;

import org.json.simple.JSONObject;

/**
 * @author Joris Basiglio
 *
 */
public interface DataListener {

	public void handle(JSONObject data);
	
}
