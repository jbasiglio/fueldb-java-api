package io.wonderfuel.fueldb.api.endpoint.websocket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.core.ReconnectTask;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;
import io.wonderfuel.fueldb.api.utils.WebUtils;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.glassfish.tyrus.client.ClientManager;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WSockClientEndpoint extends Endpoint implements IClientEndpoint {

	private Session session;
	private static WebSocketContainer container;

	private final FuelDBHandler handler;

	public WSockClientEndpoint(FuelDBHandler handler) {
		this.handler = handler;

	}

	public void connect(String host, Integer port, Boolean ssl, String user,
			String password) {
		String wsUri = "ws" + (ssl ? "s" : "") + "://" + host + ":" + port;
		if (container == null) {
			container = ClientManager.createClient();
		}
		try {
			container.connectToServer(this, ClientEndpointConfig.Builder
					.create().build(), URI.create(wsUri
					+ WebUtils.computeURL("", user, password)));
		} catch (DeploymentException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Connected with id: " + session.getId());
		this.session = session;
		handler.getOnOpen().handle(null);
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
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
		});

	}

	@Override
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("Close ..." + closeReason);
		handler.getOnClose().handle(null);
		if (closeReason.getCloseCode().getCode() < 4000) {
			ScheduledExecutorService exec = Executors
					.newSingleThreadScheduledExecutor();
			exec.schedule(new Runnable() {
				@Override
				public void run() {
					handler.connect();
				}
			}, 5, TimeUnit.SECONDS);
		}
	}

	public void send(String msg) throws IOException{
		if (session.isOpen()) {
			session.getBasicRemote().sendText(msg);
		} else {
			ScheduledExecutorService exec = Executors
					.newSingleThreadScheduledExecutor();
			exec.schedule(new ReconnectTask(this, msg), 5, TimeUnit.SECONDS);
		}
	}
}
