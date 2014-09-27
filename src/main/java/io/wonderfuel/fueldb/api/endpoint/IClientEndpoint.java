package io.wonderfuel.fueldb.api.endpoint;

import java.io.IOException;

/**
 * @author Joris Basiglio
 *
 */
public interface IClientEndpoint {
	
	public void send(String msg) throws IOException;
	
	public void connect(String host, Integer port, Boolean ssl, String user, String password) throws IOException;

	public void disconnect() throws IOException;
}
