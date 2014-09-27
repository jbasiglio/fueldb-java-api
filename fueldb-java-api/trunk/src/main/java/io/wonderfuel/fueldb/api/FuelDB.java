package io.wonderfuel.fueldb.api;

import io.wonderfuel.fueldb.api.listener.DataListener;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * {@link FuelDB} is an interface that allows you to communicate with
 * a remote fueldb server. 
 * 
 * @author Joris Basiglio
 *
 */
public interface FuelDB {

	
	/**
	 * Open the connection with the remote database instance.
	 * 
	 * @throws IOException if the server is unreachable.
	 */
	public void connect() throws IOException;
	
	/**
	 * Close the connection with the remote database instance.
	 * 
	 * @throws IOException if the server is unreachable.
	 */
	public void disconnect() throws IOException;

	/**
	 * Subscribe to a data point in the database.
	 * <p>
	 * Every time the value of the subscribed point change the {@link DataListener#handle(JSONObject)} method
	 * will be fired with the new and the old value.
	 * <p>
	 * At the subscription, the previous method will be fired with current value of the data point.
	 * 
	 * @param point is the data point you want to subscribe.
	 * @param listener is you own implementation of the callback method to handle the new value.
	 * @throws IOException if the server is unreachable.
	 * <p>
	 * Example: <br>
	 * <code>
	 * handler.subscribe("fueldb.cpu.load", new DataListener() {<br>
	 * &nbsp;public void handle(JSONObject data) {<br>
	 * &nbsp;&nbsp;System.out.println("Subs: " + data.toJSONString());<br>
	 * &nbsp;}<br>
	 * });
	 * </code>
	 */
	public void subscribe(String point, DataListener listener) throws IOException;

	/**
	 * Disable the subscription to the data point
	 * 
	 * @param point is the data point you want to unsubscribe.
	 * @throws IOException if the server is unreachable.
	 */
	public void unsubscribe(String point) throws IOException;

	/**
	 * Read a data point in the database. The listener method will be fired only one time when the server
	 * send back the answer.
	 * 
	 * @param point is the data point you want to read.
	 * @param listener is you own implementation of the callback method to handle the value.
	 * @throws IOException if the server is unreachable.
	 * <p>
	 * Example: <br>
	 * <code>
	 * handler.read("your.data.point", new DataListener() {<br>
	 * &nbsp;public void handle(JSONObject data) {<br>
	 * &nbsp;&nbsp;System.out.println("Read: " + data.get("value").toString());<br>
	 * &nbsp;}<br>
	 * });
	 * </code>
	 * 
	 */
	public void read(String point, DataListener listener) throws IOException;

	/**
	 * Perform a synchronous call to read a data point.
	 * @see FuelDB#read(String, DataListener)
	 * 
	 * @param point is the data point you want to read.
	 * @return the JSON value.
	 * @throws IOException if the server is unreachable.
	 * @throws InterruptedException if the server did not respond after 2 seconds.
	 */
	public JSONObject readSync(String point) throws IOException, InterruptedException;

	/**
	 * Browsing a data point gives you the list of all his children points.
	 * <p>
	 * Structure:<br>
	 * <ul>
	 * <li>test</li>
	 * 	<ul>
	 * 	<li>rand</li>
	 * 		<ul>
	 * 		<li>rand</li>
	 * 		</ul>
	 * 	</ul>
	 * <li>foo</li>
	 * 	<ul>
	 * 	<li>bar</li>
	 * 		<ul>
	 * 		<li>test</li>
	 * 		</ul>
	 *  <li>bar2</li>
	 * 	</ul>
	 * </ul>
	 * <p>
	 * In this example of database structure, browsing:<br>
	 * <ul>
	 * 	<li>"foo" gives you ["bar","bar2"]</li>
	 * 	<li>"foo.bar" gives you ["test"]</li>
	 * 	<li>"" gives you ["test","foo"]</li>
	 * 	<li>"test.rand.rand" gives you []</li>
	 * </ul>
	 *  
	 * @param point is the data point you want to browse.
	 * @param listener is you own implementation of the callback method to handle the child point.
	 * @throws IOException if the server is unreachable.
	 * <p>
	 * Example: <br>
	 * <code>
	 * handler.browse("your.data.point", new DataListener() {<br>
	 * &nbsp;public void handle(JSONObject data) {<br>
	 * &nbsp;&nbsp;System.out.println("Browse: " + data.toJSONString());<br>
	 * &nbsp;}<br>
	 * });
	 * </code>
	 */
	public void browse(String point, DataListener listener) throws IOException;

	/**
	 * Perform a synchronous call to browse a data point.
	 * @see FuelDB#browse(String, DataListener)
	 * 
	 * @param point is the data point you want to browse.
	 * @return a JSON array containing the children points.
	 * @throws IOException if the server is unreachable.
	 * @throws InterruptedException if the server did not respond after 2 seconds.
	 */
	public JSONObject browseSync(String point) throws IOException, InterruptedException;

	/**
	 * Write a value in the data point.
	 * 
	 * @param point is data point where you want to write the value.
	 * @param value has to be an instance of the following:
	 *	{@link JSONObject},
	 * 	{@link JSONArray},
	 * 	{@link String},
	 * 	{@link Number},
	 * 	{@link Boolean}
	 * @throws IOException if the server is unreachable.
	 */
	public void write(String point, Object value) throws IOException;

	/**
	 * Remove a data point from the remote database.
	 * 
	 * @param point you want to remove
	 * @throws IOException if the server is unreachable
	 */
	public void remove(String point) throws IOException;

	/**
	 * Assign a new listener to the close connection event
	 * 
	 * @param onClose is the callback function that will be fired when the connection is lost.
	 */
	public void setOnClose(DataListener onClose);

	/**
	 * Assign a new listener to the open connection event
	 * 
	 * @param onOpen is the callback function that will be fired when the connection is open.
	 */
	public void setOnOpen(DataListener onOpen);

}