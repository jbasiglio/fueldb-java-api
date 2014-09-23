package io.wonderfuel.fueldb.api.test;

import io.wonderfuel.fueldb.api.FuelDB;
import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.endpoint.ClientEndpointEnum;
import io.wonderfuel.fueldb.api.listener.DataListener;

import java.io.IOException;

import javax.websocket.DeploymentException;

import org.json.simple.JSONObject;

public class Connect {

	public static void main(String[] args) {
		final FuelDB handler = FuelDBHandler.get(ClientEndpointEnum.SOCKET, "127.0.0.1", 8103, false, "admin", "admin");
		try {
			handler.connect();

			handler.subscribe("fueldb.cpu.load", new DataListener() {
				@Override
				public void handle(JSONObject data) {
					System.out.println("Subs: " + data.toJSONString());

				}
			});

			JSONObject obj = handler.readSync("fueldb.cpu.load");
			System.out.println("Read Sync: " + obj.toJSONString());
		} catch (IOException | InterruptedException | DeploymentException e) {
			e.printStackTrace();
		}
	}

}
