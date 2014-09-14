package io.wonderfuel.fueldb.api.endpoint.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class SocketReader extends Thread {

	private SockClientEndpoint endpoint;
	private SocketChannel channel;
	
	private final static int BUFF_SIZE = 2048;

	public SocketReader(SockClientEndpoint endpoint, SocketChannel channel) {
		this.endpoint = endpoint;
		this.channel = channel;
	}

	@Override
	public void run() {
		try {
			while (channel.isConnected()) {
				ByteBuffer buff = ByteBuffer.allocate(BUFF_SIZE);
				String message = "";
				int r = BUFF_SIZE;
				while(buff.get(r-1) != (int)'\n'){
					buff = ByteBuffer.allocate(BUFF_SIZE);
					r = channel.read(buff);
					message += new String(buff.array(),Charset.forName("UTF-8"));			
				}
				endpoint.onMessage(message.trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
