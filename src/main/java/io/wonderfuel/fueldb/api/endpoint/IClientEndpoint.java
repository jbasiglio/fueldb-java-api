package io.wonderfuel.fueldb.api.endpoint;

import java.io.IOException;

import javax.websocket.DeploymentException;


public interface IClientEndpoint {
	
	public void send(String msg) throws IOException;
	
	public void connect(String host, Integer port, Boolean ssl, String user, String password) throws DeploymentException, IOException;
}
