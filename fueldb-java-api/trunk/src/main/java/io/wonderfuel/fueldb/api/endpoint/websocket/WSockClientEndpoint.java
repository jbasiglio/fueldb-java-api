package io.wonderfuel.fueldb.api.endpoint.websocket;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.core.ResendTask;
import io.wonderfuel.fueldb.api.endpoint.IClientEndpoint;
import io.wonderfuel.fueldb.api.utils.WebUtils;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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

/**
 * @author Joris Basiglio
 *
 */
public class WSockClientEndpoint extends Endpoint implements IClientEndpoint {

	private static final Logger LOG = Logger.getLogger(WSockClientEndpoint.class.getName());
	
	private Session session;
	private static WebSocketContainer container;

	private final FuelDBHandler handler;

	public WSockClientEndpoint(FuelDBHandler handler) {
		this.handler = handler;

	}

	public void connect(String host, Integer port, Boolean ssl, String user, String password) throws IOException {
		String wsUri = "ws" + (ssl ? "s" : "") + "://" + host + ":" + port;
		if (container == null) {
			container = ClientManager.createClient();
		}
		try {
			container.connectToServer(this, ClientEndpointConfig.Builder.create().build(), URI.create(wsUri + WebUtils.computeURL("", user, password)));
		} catch (DeploymentException e) {
			LOG.log(Level.SEVERE, "Unable to deploy websocket", e);
		}
	}
	
	@Override
	public void disconnect() throws IOException{
		session.close();
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		handler.getOnOpen().handle(null);
		session.addMessageHandler(new MessageHandler.Whole<String>() {
			@Override
			public void onMessage(String message) {
				handler.process(message);
			}
		});

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClose(Session session, CloseReason closeReason) {
		JSONObject close = new JSONObject();
		close.put("code", closeReason.getCloseCode().getCode());
		close.put("value", closeReason.getReasonPhrase());
		handler.getOnClose().handle(close);
		if (closeReason.getCloseCode().getCode() < 4000) {
			final Runnable reconnect = new Runnable() {
				@Override
				public void run() {
					try {
						handler.connect();
					} catch (Exception e) {
						e.printStackTrace();
						ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
						exec.schedule(this, 5, TimeUnit.SECONDS);
					}
				}
			};
			reconnect.run();
		}
	}

	public void send(String msg) throws IOException {
		if (session != null && session.isOpen()) {
			session.getBasicRemote().sendText(msg);
		} else {
			ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
			exec.schedule(new ResendTask(this, msg), 5, TimeUnit.SECONDS);
		}
	}
}
