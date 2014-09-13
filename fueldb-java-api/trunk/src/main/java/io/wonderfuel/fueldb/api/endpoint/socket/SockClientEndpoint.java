package io.wonderfuel.fueldb.api.endpoint.socket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.core.ReconnectTask;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SockClientEndpoint implements IClientEndpoint {

	private final FuelDBHandler handler;

	private SocketChannel channel;

	public SockClientEndpoint(FuelDBHandler handler) {
		this.handler = handler;
	}

	@Override
	public void connect(String host, Integer port, Boolean ssl, String user,
			String password) {
		try {
			if (channel == null) {
				channel = SocketChannel.open(new InetSocketAddress(host, port));
			}
			SocketReader reader = new SocketReader(this, channel);
			reader.start();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void onOpen() {
		System.out.println("Connected ... ");
		handler.getOnOpen().handle(null);
	}
	
	
	public void onMessage(String message) {
		JSONObject obj = null;
		try {
			obj = (JSONObject) new JSONParser().parse(message);
		} catch (ParseException e) {
			e.printStackTrace();
			return;
		}
		if (obj.get("id") == null) {
			handler.getSusbscribes().get((String) obj.get("point"))
					.handle(obj);
		} else {
			String point = (String) obj.get("id");
			handler.getCallbacks().get(point).handle(obj);
			handler.getCallbacks().remove(point);
		}

	}

	public void onClose() {
		System.out.println("Close ...");
		handler.getOnClose().handle(null);
	}

	@Override
	public void send(String msg) throws IOException {
		if (channel.isConnected()) {
			ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
			while (buf.hasRemaining()) {
				channel.write(buf);
			}
		} else {
			System.out.println("Not connected... delaying");
			ScheduledExecutorService exec = Executors
					.newSingleThreadScheduledExecutor();
			exec.schedule(new ReconnectTask(this, msg), 5, TimeUnit.SECONDS);
		}
	}
}
