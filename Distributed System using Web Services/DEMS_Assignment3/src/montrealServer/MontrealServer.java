/**
 * 
 */
package montrealServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.ws.Endpoint;

/**
 * @author Yogesh Nimbhorkar
 *
 */
public class MontrealServer {

	public static void main(String[] args) throws Exception {

		try {

			MontrealServerImplementation monStub = new MontrealServerImplementation();
			Endpoint endpoint = Endpoint.publish("http://localhost:8080/invokeMontrealServer", monStub);
			new Thread(() -> receiverequest(monStub)).start();

			System.out.println("Montreal City Server ready and waiting ...");
		}

		catch (Exception e) {

			e.printStackTrace();

		}

	}

	/**
	 * Method to recieve request by other servers
	 */
	static void receiverequest(MontrealServerImplementation monStub) {
		monStub.LOG.info("Request received at Montreal server.");
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(7777);
			System.out.println("Server 7777 Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				String inputData = new String(request.getData());
				DatagramPacket reply = null;

				String message = "";

				String input[] = inputData.split(",");

				String operation = input[0];
				String eventType = input[1];
				String eventId = input[2];
				int port = Integer.parseInt(input[3]);
				String customerId = input[4];
				int bookingCapacity = Integer.parseInt(input[5].trim());
				String oldEventType= input[6];
				String oldEventId= input[7];
				customerId = customerId.trim();
				monStub.LOG.info("Montreal server received request  " + operation + " with Event Type: " + eventType
						+ " Event Id: " + eventId + " Server Port: " + port + " User Id: " + customerId + " Capacity: "
						+ bookingCapacity+ " Old Event Type: " + oldEventType + " Old Event Id: " + oldEventId);

//				For only adding event to evenInfo map by manager
				if (operation.equalsIgnoreCase("addeventmanager")) {
					monStub.LOG.info("Adding new event at montreal server by manager");
					message = "";
					message = monStub.addEvent(eventId, eventType, bookingCapacity);
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For only removing event from userInfo map and not updating capacity as event is deleted by manager
				if (operation.equalsIgnoreCase("removeuserevent")) {
					message = "";
					monStub.LOG.info("---------Request recieved to remove event from user map---------");

					for (Map.Entry<String, LinkedHashMap<String, ArrayList<String>>> userInfoEntry : monStub.userInfo
							.entrySet()) {
						if (userInfoEntry.getValue().containsKey(eventType)) {
							if (userInfoEntry.getValue().get(eventType).contains(eventId)) {
								userInfoEntry.getValue().get(eventType).remove(eventId);
								message = "event successfully removed from user list of montreal server";
							}
						}
					}
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For only deleting event from eventInfo only by manager
				else if (operation.equalsIgnoreCase("deleteevent")) {
					monStub.LOG.info("---------Request recieved to delete the event by manager---------");
					message = "";
					message = monStub.removeEvent(eventType, eventId);
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For only listing availability of event from eventInfo only by manager
				else if (operation.equalsIgnoreCase("listeventmanager")) {
					monStub.LOG.info(
							"---------Request recieved to lsit the availability of the event by manager---------");
					message = "";
					for (Map.Entry<String, LinkedHashMap<String, Integer>> eventInfoEntry : monStub.eventInfo
							.entrySet()) {
						if (eventInfoEntry.getKey().equalsIgnoreCase(eventType)) {
							for (Entry<String, Integer> currentEvenEntry : eventInfoEntry.getValue().entrySet()) {
								message = message + "Event Id: " + currentEvenEntry.getKey() + "    Capacity: "
										+ currentEvenEntry.getValue() + " \n";
							}
						}
					}
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For booking event by customer only
				else if (operation.equalsIgnoreCase("bookevent")) {
					monStub.LOG.info("---------Request recieved to book the new event---------");
					message = "";
					message = monStub.bookEvent(customerId, eventType, eventId);
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For getting booking schedule of perticular customer
				else if (operation.equalsIgnoreCase("getbookingschedule")) {
					monStub.LOG.info("---------Request recieved to get the booking schedule of user---------");
					message = "";
					if (monStub.userInfo.containsKey(customerId)) {

						for (Map.Entry<String, ArrayList<String>> entry : monStub.userInfo.get(customerId).entrySet()) {
							message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue()
									+ " \n";
						}
					}
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

//				For caceling event from perticular customer. Just increasing capacity of perticular event type and event id
				else if (operation.equalsIgnoreCase("cancelevent")) {
					monStub.LOG.info("---------Request recieved to cancel the event---------");
					message = "";
					if (monStub.eventInfo.containsKey(eventType)) {
						if (monStub.eventInfo.get(eventType).containsKey(eventId)) {
							int currentCapacity = monStub.eventInfo.get(eventType).get(eventId);
							monStub.eventInfo.get(eventType).put(eventId, currentCapacity + 1);
							message = "Event capacity has increased after cancelling event";
						} else
							message = "Event not found in montreal server.";
					}
					monStub.LOG.info("Respond send by montreal server :" + message);
				}
				
//				For Checking if customer has booking for particular event or not
				else if (operation.equalsIgnoreCase("checkoldevent")) {
					monStub.LOG.info("---------Request recieved to check if old event is present or not---------");
					message = "";
					if (monStub.userInfo.containsKey(customerId)) {
						for (Map.Entry<String, ArrayList<String>> eventEntry : monStub.userInfo.get(customerId).entrySet()) {
							if (eventEntry.getValue().contains(oldEventId) && eventEntry.getKey().equalsIgnoreCase(oldEventType)) {
								message = "success";
								break;
							}
						}
					}
					monStub.LOG.info("Respond send by montreal server :" + message);
				}
//				For swapping user event
				else if (operation.equalsIgnoreCase("swapevent")) {
					monStub.LOG.info("---------Request recieved to swap event---------");
					message = "";
					message = monStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
					monStub.LOG.info("Respond send by montreal server :" + message);
				}
//				For validation user event
				else if (operation.equalsIgnoreCase("checkifvalid")) {
					monStub.LOG.info("---------Request recieved to check for validity of event---------");
					message = "";
					if (monStub.eventInfo.containsKey(eventType)) {
						if (monStub.eventInfo.get(eventType).containsKey(eventId)) {
							if (monStub.eventInfo.get(eventType).get(eventId)>0) {
								message = "success";
							}
						}
					}
					
					monStub.LOG.info("Respond send by montreal server :" + message);
				}

				byte[] resultMessage = message.getBytes();
				reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(), request.getPort());

				monStub.LOG.info("Result is: " + message);
				System.out.println(message);
				monStub.LOG.info("Sending reply");
				aSocket.send(reply);
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}

}
