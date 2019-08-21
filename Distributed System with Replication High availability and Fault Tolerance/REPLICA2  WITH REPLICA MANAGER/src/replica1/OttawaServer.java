/**
 * @author Yogesh Nimbhorkar
 */
package replica1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Yogesh
 *
 */
public class OttawaServer {

	public static Thread otwThread=null;
	 public volatile static boolean running;
	 static DatagramSocket aSocket = null;
	 static OttawaServerImplementation otwStub ;
	 
    public static void startOttawaServer() throws Exception {

		try {
			otwStub = new OttawaServerImplementation();
			System.out.println("Ottawa City Server ready and waiting ...");
			otwThread=new Thread(() -> receiverequest(otwStub));
			otwThread.start();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

    
    public static void stopServer() {
    	 if (otwThread == null) return;
         running = false;
         aSocket.close();
         System.out.println("Ottawa server shutting down");
	}
    
    
    /**
     *	Method to recieve request by other servers
     */
    static void receiverequest(OttawaServerImplementation otwStub) {
	otwStub.LOG.info("Request received at ottawa server.");
	try {
	    aSocket = new DatagramSocket(6666);
	    System.out.println("Server 6666 Started............");
	    running=true;
	    while (running) {
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
		otwStub.LOG.info("Ottawa server received request  " + operation + " with Event Type: " + eventType
				+ " Event Id: " + eventId + " Server Port: " + port + " User Id: " + customerId + " Capacity: "
				+ bookingCapacity+ " Old Event Type: " + oldEventType + " Old Event Id: " + oldEventId);
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
//		For Checking if customer has booking for particular event or not
		else if (operation.equalsIgnoreCase("checkoldevent")) {
			otwStub.LOG.info("---------Request recieved to check if old event is present or not---------");
			message = "";
			if (otwStub.userInfo.containsKey(customerId)) {
				for (Map.Entry<String, ArrayList<String>> eventEntry : otwStub.userInfo.get(customerId).entrySet()) {
					if (eventEntry.getValue().contains(oldEventId) && eventEntry.getKey().equalsIgnoreCase(oldEventType)) {
						message = "success";
						break;
					}
				}
			}
			otwStub.LOG.info("Respond send by montreal server :" + message);
		}
//		For swapping user event
		else if (operation.equalsIgnoreCase("swapevent")) {
			otwStub.LOG.info("---------Request recieved to swap event---------");
			message = "";
			message = otwStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
			otwStub.LOG.info("Respond send by ottawa server :" + message);
		}
//		For swapping user event
		else if (operation.equalsIgnoreCase("checkifvalid")) {
			otwStub.LOG.info("---------Request recieved to check for validity of event---------");
			message = "";
			if (otwStub.eventInfo.containsKey(eventType)) {
				if (otwStub.eventInfo.get(eventType).containsKey(eventId)) {
					if (otwStub.eventInfo.get(eventType).get(eventId)>0) {
						message = "success";
					}
				}
			}
			
			otwStub.LOG.info("Respond send by ottawa server :" + message);
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