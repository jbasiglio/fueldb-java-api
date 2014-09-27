package io.wonderfuel.fueldb.api.core;

import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Joris Basiglio
 *
 */
public class ResendTask implements Runnable {

	private final static Logger LOG = Logger.getLogger(ResendTask.class.getName());
	
	private IClientEndpoint endpoint;
	private String msg;
	
	public ResendTask(IClientEndpoint endpoint, String msg){
		this.endpoint = endpoint;
		this.msg = msg;
	}
	
	@Override
	public void run() {
		try {
			endpoint.send(msg);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Can't send data", e);
		}
	}

}
