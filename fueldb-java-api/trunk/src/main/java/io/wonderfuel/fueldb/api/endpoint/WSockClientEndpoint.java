package io.wonderfuel.fueldb.api.endpoint;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WSockClientEndpoint extends Endpoint {

	private Session session;

	private final FuelDBHandler handler;

	public WSockClientEndpoint(FuelDBHandler handler) {
		this.handler = handler;
	}

	@Override
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Connected ... " + session.getId());
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
					handler.getSusbscribes().get((String)obj.get("point")).handle(obj);
				} else {
					String point = (String)obj.get("id");
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
			ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
			exec.schedule(new Runnable() {
				@Override
				public void run() {
					handler.connect();
				}
			}, 5, TimeUnit.SECONDS);
		}
	}

	public void send(String msg) {
		try {
			if (session.isOpen()) {
				session.getBasicRemote().sendText(msg);
			}else{
				ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
				exec.schedule(new Runnable() {
					@Override
					public void run() {
						//TODO
					}
				}, 5, TimeUnit.SECONDS);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
