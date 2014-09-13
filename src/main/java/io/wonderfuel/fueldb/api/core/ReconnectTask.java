package io.wonderfuel.fueldb.api.core;

import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;

import java.io.IOException;

public class ReconnectTask implements Runnable {

	private IClientEndpoint endpoint;
	private String msg;
	
	public ReconnectTask(IClientEndpoint endpoint, String msg){
		this.endpoint = endpoint;
		this.msg = msg;
	}
	
	@Override
	public void run() {
		try {
			endpoint.send(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
