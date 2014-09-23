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

public class SockClientEndpoint implements IClientEndpoint {

	private final FuelDBHandler handler;

	private Socket socket;

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
			handler.getOnOpen().handle(null);
			SocketReader reader = new SocketReader(handler, socket.getInputStream());
			reader.start();
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
