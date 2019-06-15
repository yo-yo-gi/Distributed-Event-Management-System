/**
 * 
 */
package ottawaServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Yogesh
 *
 */
public class OttawaServer {

    public static void main(String[] args) throws Exception {

	OttawaServerImplementation otwStub = new OttawaServerImplementation();

	Registry registry = LocateRegistry.createRegistry(3535);
	registry.bind("ottawaStub", otwStub);

	System.out.println("Ottawa city server has been started successfully");

	new Thread(() -> receiverequest(otwStub)).start();

    }

    /**
     *	Method to recieve request by other servers
     */
    static void receiverequest(OttawaServerImplementation otwStub) {
	otwStub.LOG.info("Request received at ottawa server.");
	DatagramSocket aSocket = null;
	try {
	    aSocket = new DatagramSocket(6666);
	    System.out.println("Server 6666 Started............");
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
		customerId = customerId.trim();
		otwStub.LOG.info("Ottawa server received request  "+operation+" with Event Type: "+eventType+" Event Id: "+eventId+" Server Port: "+port+" User Id: "+customerId+" Capacity: "+bookingCapacity);

//				For only adding event to evenInfo map by manager
		if (operation.equalsIgnoreCase("addeventmanager")) {
		    message = "";
		    message = otwStub.addEvent(eventId, eventType, bookingCapacity);
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For only removing event from userInfo map and not updating capacity as event is deleted by manager
		if (operation.equalsIgnoreCase("removeuserevent")) {
		    message = "";
		    otwStub.LOG.info("---------Request recieved to remove event from user map---------");

		    for (Map.Entry<String, LinkedHashMap<String, ArrayList<String>>> userInfoEntry : otwStub.userInfo
			    .entrySet()) {
			if (userInfoEntry.getValue().containsKey(eventType)) {
			    if (userInfoEntry.getValue().get(eventType).contains(eventId)) {
				userInfoEntry.getValue().get(eventType).remove(eventId);
				message = "event successfully removed from user list of montreal server";
			    }
			}
		    }
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For only deleting event from eventInfo only by manager
		else if (operation.equalsIgnoreCase("deleteevent")) {
		    otwStub.LOG.info("---------Request recieved to delete the event by manager---------");
		    message = "";
		    message = otwStub.removeEvent(eventType, eventId);
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For only listing availability of event from eventInfo only by manager
		else if (operation.equalsIgnoreCase("listeventmanager")) {
		    otwStub.LOG.info("---------Request recieved to lsit the availability of the event by manager---------");
		    message = "";
		    for (Map.Entry<String, LinkedHashMap<String, Integer>> eventInfoEntry : otwStub.eventInfo
			    .entrySet()) {
			if (eventInfoEntry.getKey().equalsIgnoreCase(eventType)) {
			    for (Entry<String, Integer> currentEvenEntry : eventInfoEntry.getValue().entrySet()) {
				message = message + "Event Id: " + currentEvenEntry.getKey() + "    Capacity: "
					+ currentEvenEntry.getValue() + " \n";
			    }
			}
		    }
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For booking event by customer only
		else if (operation.equalsIgnoreCase("bookevent")) {
		    otwStub.LOG.info("---------Request recieved to book the new event---------");
		    message = "";
		    message = otwStub.bookEvent(customerId, eventType, eventId);
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For getting booking schedule of perticular customer
		else if (operation.equalsIgnoreCase("getbookingschedule")) {
		    otwStub.LOG.info("---------Request recieved to get the booking schedule of user---------");
		    message = "";
		    if (otwStub.userInfo.containsKey(customerId)) {

			for (Map.Entry<String, ArrayList<String>> entry : otwStub.userInfo.get(customerId).entrySet()) {
			    message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue()
				    + " \n";
			}
		    }
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

//				For caceling event from perticular customer. Just increasing capacity of perticular event type and event id
		else if (operation.equalsIgnoreCase("cancelevent")) {
		    otwStub.LOG.info("---------Request recieved to cancel the event---------");
		    message = "";
		    if (otwStub.eventInfo.containsKey(eventType)) {
			if (otwStub.eventInfo.get(eventType).containsKey(eventId)) {
			    int currentCapacity = otwStub.eventInfo.get(eventType).get(eventId);
			    otwStub.eventInfo.get(eventType).put(eventId, currentCapacity + 1);
			    message = "Event capacity has increased after cancelling event";
			} else
			    message = "Event not found in montreal server.";
		    }
		    otwStub.LOG.info("Respond send by otawa server :"+message);
		}

		byte[] resultMessage = message.getBytes();
		reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(), request.getPort());

		otwStub.LOG.info("Result is: " + message);
		System.out.println(message);
		otwStub.LOG.info("Sending reply////");
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
