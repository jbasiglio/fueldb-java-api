package io.wonderfuel.fueldb.api.endpoint.socket;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

/**
 * @author Joris Basiglio
 *
 */
public class SocketReader extends Thread {

	private final static Logger LOG = Logger.getLogger(SocketReader.class.getName());
	
	private SockClientEndpoint endpoint;
	
	private InputStream stream;

	private final static int BUFF_SIZE = 2048;

	public SocketReader(SockClientEndpoint endpoint, InputStream stream) {
		this.endpoint = endpoint;
		this.stream = stream;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			while (endpoint.isConnected()) {
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
				String[] array = message.split("\3");
				for (String part : array) {
					if(!part.trim().isEmpty()){
						endpoint.process(part.trim());
					}
				}
				
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Error when reading the socket", e);
			JSONObject error = new JSONObject();
			error.put("point", ".ERROR");
			error.put("value", e.getLocalizedMessage());
			endpoint.process(error.toJSONString());
		}
		endpoint.onReaderEnd();
	}

}
