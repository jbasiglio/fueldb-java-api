package io.wonderfuel.fueldb.api.endpoint.socket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.json.simple.JSONObject;

public class SocketReader extends Thread {

	private FuelDBHandler handler;
	//private SockClientEndpoint endpoint;
	private InputStream stream;

	private final static int BUFF_SIZE = 2048;

	public SocketReader(FuelDBHandler handler, InputStream stream) {
		this.handler = handler;
		//this.endpoint = endpoint;
		this.stream = stream;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while (true) {
				byte[] buff = new byte[BUFF_SIZE];
				String message = "";
				int r = BUFF_SIZE;
				while (buff[(r - 1)] != (int) '\3') {
					Arrays.fill(buff, (byte) 0);
					r = stream.read(buff);
					if(r<0){
						r = BUFF_SIZE;
						continue;
					}
					message += new String(buff, Charset.forName("UTF-8"));
				}
				handler.process(message.trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject error = new JSONObject();
			error.put("point", ".ERROR");
			error.put("value", e.getLocalizedMessage());
			handler.process(error.toJSONString());
		}
		handler.getOnClose().handle(null);
	}

}
