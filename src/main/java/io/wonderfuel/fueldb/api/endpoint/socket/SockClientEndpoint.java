package io.wonderfuel.fueldb.api.endpoint.socket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.core.ResendTask;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
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
	private PrintWriter pw;

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
		pw =  new PrintWriter(socket.getOutputStream());
		handler.getOnOpen().handle(null);
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.execute(new SocketReader(this, socket));
	}
	
	public void process(String msg){
		handler.process(msg);
	}
	
	public void onReaderEnd(){
		handler.getOnClose().handle(null);
	}
	
	@Override
	public void disconnect() throws IOException{
		socket.close();
	}

	@Override
	public void send(String msg) throws IOException {
		if (socket.isConnected()) {
			msg+="\3";
			pw.print(msg);
			pw.flush();
		} else {
			ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
			exec.schedule(new ResendTask(this, msg), 1, TimeUnit.SECONDS);
		}
	}
}
