package io.wonderfuel.fueldb.api.core;

import io.wonderfuel.fueldb.api.FuelDB;
import io.wonderfuel.fueldb.api.endpoint.ClientEndpointEnum;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;
import io.wonderfuel.fueldb.api.listener.DataListener;
import io.wonderfuel.fueldb.api.utils.Wrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.DeploymentException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FuelDBHandler implements FuelDB {

	private static final Logger LOG = Logger.getLogger(FuelDBHandler.class.getName());
	
	private String host;
	private Integer port;
	private Boolean ssl;
	private String user;
	private String password;

	private IClientEndpoint endpoint;

	private Map<String, DataListener> susbscribes = Collections.synchronizedMap(new HashMap<String, DataListener>());
	private Map<String, DataListener> callbacks = Collections.synchronizedMap(new HashMap<String, DataListener>());

	private Integer counter = 0;

	private DataListener onClose = new DataListener() {

		@Override
		public void handle(JSONObject data) {
			LOG.log(Level.INFO, "Close handler");
		}
	};
	private DataListener onOpen = new DataListener() {

		@Override
		public void handle(JSONObject data) {
			LOG.log(Level.INFO, "Open handler");
		}
	};

	public static FuelDB get(ClientEndpointEnum type, String host, Integer port, Boolean ssl, String password, String user) {
		return new FuelDBHandler(type, host, port, ssl, password, user);
	}

	private FuelDBHandler(ClientEndpointEnum type, String host, Integer port, Boolean ssl, String password, String user) {

		this.host = host;
		this.port = port;
		this.ssl = ssl;
		this.user = user;
		this.password = password;
		callbacks.put(".ERROR", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				LOG.log(Level.SEVERE, data.toJSONString());
			}
		});
		callbacks.put(".WARNING", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				LOG.log(Level.WARNING, data.toJSONString());
			}
		});
		callbacks.put(".INFO", new DataListener() {
			@Override
			public void handle(JSONObject data) {
				LOG.log(Level.INFO, data.toJSONString());
			}
		});
		endpoint = type.instance(this);
	}

	@Override
	public void connect() throws DeploymentException, IOException {
		endpoint.connect(host, port, ssl, user, password);
	}

	public void process(String message) {
		JSONObject obj = null;
		try {
			obj = (JSONObject) new JSONParser().parse(message);
		} catch (ParseException e) {
			LOG.log(Level.SEVERE, "Unable to parse response", e);
			return;
		}
		String id = (String) obj.get("id");
		String point = (String) obj.get("point");
		if (id == null && point.indexOf('.') != 0) {
			susbscribes.get(point).handle(obj);
		} else if (id == null && (point).indexOf('.') == 0) {
			callbacks.get(point).handle(obj);
		} else {
			callbacks.get(id).handle(obj);
			callbacks.remove(id);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void subscribe(String point, DataListener listener) throws IOException {
		susbscribes.put(point, listener);
		JSONObject obj = new JSONObject();
		obj.put("type", "subscribe");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void unsubscribe(String point) throws IOException {
		susbscribes.remove(point);
		JSONObject obj = new JSONObject();
		obj.put("type", "unsubscribe");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void read(String point, final DataListener listener) throws IOException {
		final String uid = getUID();
		callbacks.put(uid, new DataListener() {
			@Override
			public void handle(JSONObject data) {
				callbacks.remove(uid);
				listener.handle(data);
			}
		});
		JSONObject obj = new JSONObject();
		obj.put("type", "read");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject readSync(String point) throws IOException, InterruptedException {
		final Wrapper<JSONObject> response = new Wrapper<>();
		String uid = getUID();
		callbacks.put(uid, new DataListener() {
			@Override
			public void handle(JSONObject data) {
				response.setValue(data);
				synchronized (response) {
					response.notify();
				}
			}
		});
		JSONObject obj = new JSONObject();
		obj.put("type", "read");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
		synchronized (response) {
			response.wait(2000L);
		}
		callbacks.remove(uid);
		return response.getValue();
	}

	/*
	 * public JSONObject readHTTP(String point){ try { URL url = new URL(httpUri
	 * + WebUtils.computeURL(point,user,password)); HttpURLConnection con =
	 * (HttpURLConnection) url.openConnection(); con.setRequestMethod("GET");
	 * return (JSONObject) new JSONParser().parse(new
	 * InputStreamReader(con.getInputStream())); } catch (Exception e) {
	 * e.printStackTrace(); } return null; }
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void browse(String point, final DataListener listener) throws IOException {
		final String uid = getUID();
		callbacks.put(uid, new DataListener() {
			@Override
			public void handle(JSONObject data) {
				callbacks.remove(uid);
				listener.handle(data);
			}
		});
		JSONObject obj = new JSONObject();
		obj.put("type", "browse");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
	}

	@Override
	@SuppressWarnings("unchecked")
	public JSONObject browseSync(String point) throws IOException, InterruptedException {
		final Wrapper<JSONObject> response = new Wrapper<>();
		String uid = getUID();
		callbacks.put(uid, new DataListener() {
			@Override
			public void handle(JSONObject data) {
				response.setValue(data);
				synchronized (response) {
					response.notify();
				}
			}
		});
		JSONObject obj = new JSONObject();
		obj.put("type", "browse");
		obj.put("point", point);
		obj.put("id", uid);
		endpoint.send(obj.toJSONString());
		synchronized (response) {
			response.wait(2000L);
		}
		callbacks.remove(uid);
		return response.getValue();
	}

	/*
	 * public JSONObject browseHTTP(String point){ try { URL url = new
	 * URL(httpUri + WebUtils.computeURL(point,user,password));
	 * HttpURLConnection con = (HttpURLConnection) url.openConnection();
	 * con.setRequestMethod("POST"); return (JSONObject) new
	 * JSONParser().parse(new InputStreamReader(con.getInputStream())); } catch
	 * (Exception e) { e.printStackTrace(); } return null; }
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void write(String point, JSONObject value) throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("type", "set");
		obj.put("point", point);
		obj.put("value", value);
		endpoint.send(obj.toJSONString());
	}

	/*
	 * public void writeHTTP(String point, JSONObject value) { try { URL url =
	 * new URL(httpUri + WebUtils.computeURL(point,user,password));
	 * HttpURLConnection con = (HttpURLConnection) url.openConnection();
	 * con.setRequestMethod("PUT");con.setDoOutput(true); DataOutputStream wr =
	 * new DataOutputStream(con.getOutputStream());
	 * wr.writeBytes("value="+value.toJSONString()); wr.flush(); wr.close();
	 * con.getResponseCode(); } catch (Exception e) { e.printStackTrace(); } }
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void remove(String point) throws IOException {
		JSONObject obj = new JSONObject();
		obj.put("type", "remove");
		obj.put("point", point);
		endpoint.send(obj.toJSONString());
	}

	/*
	 * public void removeHTTP(String point) { try { URL url = new URL(httpUri +
	 * WebUtils.computeURL(point,user,password)); HttpURLConnection con =
	 * (HttpURLConnection) url.openConnection(); con.setRequestMethod("DELETE");
	 * con.getResponseCode(); } catch (Exception e) { e.printStackTrace(); } }
	 */

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

	@Override
	public void setOnClose(DataListener onClose) {
		this.onClose = onClose;
	}

	public DataListener getOnOpen() {
		return onOpen;
	}

	@Override
	public void setOnOpen(DataListener onOpen) {
		this.onOpen = onOpen;
	}
}
