package io.wonderfuel.fueldb.api.endpoint;

import io.wonderfuel.fueldb.api.core.FuelDBHandler;
import io.wonderfuel.fueldb.api.endpoint.socket.SockClientEndpoint;
import io.wonderfuel.fueldb.api.endpoint.websocket.WSockClientEndpoint;

/**
 * @author Joris Basiglio
 *
 */
public enum ClientEndpointEnum {

	SOCKET{
		public IClientEndpoint instance(FuelDBHandler handler){
			return new SockClientEndpoint(handler);
		}
	},
	WEBSOCKET{
		public IClientEndpoint instance(FuelDBHandler handler){
			return new WSockClientEndpoint(handler);
		}
	};
	
	public abstract IClientEndpoint instance(FuelDBHandler handler);
	
	
}
