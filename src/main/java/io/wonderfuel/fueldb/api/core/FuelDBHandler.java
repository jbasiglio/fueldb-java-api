package io.wonderfuel.fueldb.api.core;

import io.wonderfuel.fueldb.api.endpoint.WSockClientEndpoint;
import io.wonderfuel.fueldb.api.listener.DataListener;
import io.wonderfuel.fueldb.api.utils.CypherUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FuelDBHandler {

	private String uri;
	private String user;
	private String password;

	private String wsUri;
	private String httpUri;

	private WSockClientEndpoint endpoint;

	private Map<String, DataListener> susbscribes = Collections
			.synchronizedMap(new HashMap<String, DataListener>());
	private Map<String, DataListener> callbacks = Collections
			.synchronizedMap(new HashMap<String, DataListener>());

	private Integer counter = 0;

	private DataListener onClose = new DataListener() {

		@Override
		public void handle(JSONObject data) {
			System.out.println("Close Handler");
		}
	};
	private DataListener onOpen = new DataListener() {

		@Override
		public void handle(JSONObject data) {
			System.out.println("Open Handler");
		}
	};

	public FuelDBHandler(String uri, Boolean ssl, String password, String user) {
		this.uri = uri;
		this.wsUri = "ws" + (ssl ? "s" : "") + "://" + this.uri;
		this.httpUri = "http" + (ssl ? "s" : "") + "://" + this.uri;
		this.user = user;
		this.password = password;
		callbacks.put(".ERROR", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				System.out.println("[ERROR] " + data.toJSONString());
			}
		});
		callbacks.put(".WARNING", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				System.out.println("[WARNING] " + data.toJSONString());
			}
		});
		callbacks.put(".INFO", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				System.out.println("[INFO] " + data.toJSONString());
			}
		});
		connect();
	}

	private String computeURL(String point) {
		String toSign = "/" + point + "?timestamp=" + new Date().getTime()
				+ "&user=" + user;
		String key = CypherUtil.hash(password, user);
		String sign = CypherUtil.hash(toSign, key);
		return toSign + "&signature=" + sign;
	}

	public void connect() {
		try {
			WebSocketContainer container = ClientManager.createClient();
			if (endpoint == null) {
				endpoint = new WSockClientEndpoint(this);
			}
			container.connectToServer(endpoint, ClientEndpointConfig.Builder
					.create().build(), URI.create(this.wsUri + computeURL("")));
		} catch (DeploymentException | IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void subscribe(String point, DataListener listener) {
		susbscribes.put(point, listener);
		JSONObject obj = new JSONObject();
		obj.put("type", "subscribe");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}
	
	@SuppressWarnings("unchecked")
	public void unsubscribe(String point) {
		susbscribes.remove(point);
		JSONObject obj = new JSONObject();
		obj.put("type", "unsubscribe");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}

	@SuppressWarnings("unchecked")
	public void read(String point, DataListener listener) {
		String uid = getUID();
		callbacks.put(uid, listener);
		JSONObject obj = new JSONObject();
		obj.put("type", "read");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
	}

	public JSONObject readSync(String point) {
		try {
			URL url = new URL(httpUri + computeURL(point));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			return (JSONObject) new JSONParser().parse(new InputStreamReader(con.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void browse(String point, DataListener listener) {
		String uid = getUID();
		callbacks.put(uid, listener);
		JSONObject obj = new JSONObject();
		obj.put("type", "browse");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
	}
	
	public JSONObject browseSync(String point) {
		try {
			URL url = new URL(httpUri + computeURL(point));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			return (JSONObject) new JSONParser().parse(new InputStreamReader(con.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public void write(String point, JSONObject value) {
		JSONObject obj = new JSONObject();
		obj.put("type", "set");
		obj.put("point", point);
		obj.put("value", value);
		endpoint.send(obj.toJSONString());
	}
	
	public void writeSync(String point, JSONObject value) {
		try {
			URL url = new URL(httpUri + computeURL(point));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("PUT");con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes("value="+value.toJSONString());
			wr.flush();
			wr.close();
			con.getResponseCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void remove(String point) {
		JSONObject obj = new JSONObject();
		obj.put("type", "remove");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}
	
	public void removeSync(String point) {
		try {
			URL url = new URL(httpUri + computeURL(point));
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("DELETE");
			con.getResponseCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getUID() {
		if (counter.equals(Integer.MAX_VALUE)) {
			counter = 0;
		}
		return String.valueOf(counter++);
	}

	public Map<String, DataListener> getSusbscribes() {
		return susbscribes;
	}

	public Map<String, DataListener> getCallbacks() {
		return callbacks;
	}

	public DataListener getOnClose() {
		return onClose;
	}

	public void setOnClose(DataListener onClose) {
		this.onClose = onClose;
	}

	public DataListener getOnOpen() {
		return onOpen;
	}

	public void setOnOpen(DataListener onOpen) {
		this.onOpen = onOpen;
	}
}
