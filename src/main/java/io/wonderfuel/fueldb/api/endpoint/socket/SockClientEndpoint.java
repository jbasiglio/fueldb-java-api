package io.wonderfuel.fueldb.api.endpoint.socket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.core.ResendTask;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

/**
 * @author Joris Basiglio
 *
 */
public class SockClientEndpoint implements IClientEndpoint {

	private final FuelDBHandler handler;

	private Socket socket;
	
	private SocketReader reader;
	
	private Boolean connected = false;

	public SockClientEndpoint(FuelDBHandler handler) {
		this.handler = handler;
	}

	@Override
	public void connect(String host, Integer port, Boolean ssl, String user,
			String password) throws IOException {
			if (socket == null && (ssl == null || !ssl)) {
				socket = new Socket(host, port);
			}else if(socket == null && (ssl != null && ssl)){
				socket = SSLSocketFactory.getDefault().createSocket(host, port);
			}
			connected = true;
			handler.getOnOpen().handle(null);
			reader = new SocketReader(this, socket.getInputStream());
			reader.start();
	}
	
	public void process(String msg){
		handler.process(msg);
	}
	
	public void onReaderEnd(){
		handler.getOnClose().handle(null);
	}
	
	@Override
	public void disconnect() throws IOException{
		connected = false;
		// Nothing to do
	}
	
	public Boolean isConnected(){
		return connected;
	}

	@Override
	public void send(String msg) throws IOException {
		if (socket.isConnected()) {
			byte[] buff = msg.getBytes();
			socket.getOutputStream().write(buff);
		} else {
			ScheduledExecutorService exec = Executors
					.newSingleThreadScheduledExecutor();
			exec.schedule(new ResendTask(this, msg), 1, TimeUnit.SECONDS);
		}
	}
}
