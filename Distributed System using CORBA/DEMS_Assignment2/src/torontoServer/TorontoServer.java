/**
 * 
 */
package torontoServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;

import IDL.IDLInterface;
import IDL.IDLInterfaceHelper;

/**
 * @author Yogesh
 *
 */
public class TorontoServer {

    public static void main(String[] args) throws Exception {

		try {

			ORB orb = ORB.init(args, null);
			POA rootpoa = (POA) orb.resolve_initial_references("RootPOA");
			rootpoa.the_POAManager().activate();
			TorontoServerImplementation torStub = new TorontoServerImplementation();
			torStub.setORB(orb);
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(torStub);
			IDLInterface href = IDLInterfaceHelper.narrow(ref);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			NameComponent path[] = ncRef.to_name("torontoStub");
			ncRef.rebind(path, href);
			System.out.println("Toronto City Server ready and waiting ...");
			for (;;) {
				new Thread(() -> receiverequest(torStub)).start();
				orb.run();

			}

		}

		catch (Exception e) {

			e.printStackTrace();

		}

	}

    /**
     *	Method to recieve request by other servers
     */
    static void receiverequest(TorontoServerImplementation torStub) {
	torStub.LOG.info("Request received at toronto server.");
	DatagramSocket aSocket = null;
	try {
	    aSocket = new DatagramSocket(5555);
	    System.out.println("Server 5555 Started............");
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
		torStub.LOG.info("Toronto server received request  " + operation + " with Event Type: " + eventType
				+ " Event Id: " + eventId + " Server Port: " + port + " User Id: " + customerId + " Capacity: "
				+ bookingCapacity+ " Old Event Type: " + oldEventType + " Old Event Id: " + oldEventId);
		customerId = customerId.trim();
		torStub.LOG.info("Toronto server received request  "+operation+" with Event Type: "+eventType+" Event Id: "+eventId+" Server Port: "+port+" User Id: "+customerId+" Capacity: "+bookingCapacity);

//				For only adding event to evenInfo map by manager
		if (operation.equalsIgnoreCase("addeventmanager")) {
		    torStub.LOG.info("Adding new event at montreal server by manager");
		    message = "";
		    message = torStub.addEvent(eventId, eventType, bookingCapacity);
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For only removing event from userInfo map and not updating capacity as event is deleted by manager
		if (operation.equalsIgnoreCase("removeuserevent")) {
		    torStub.LOG.info("---------Request recieved to remove event from user map---------");
		    message = "";
		    torStub.LOG.info("---------Request recieved to remove event from user map---------");

		    for (Map.Entry<String, LinkedHashMap<String, ArrayList<String>>> userInfoEntry : torStub.userInfo
			    .entrySet()) {
			if (userInfoEntry.getValue().containsKey(eventType)) {
			    if (userInfoEntry.getValue().get(eventType).contains(eventId)) {
				userInfoEntry.getValue().get(eventType).remove(eventId);
				message = "event successfully removed from user list of montreal server";
			    }
			}
		    }
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For only deleting event from eventInfo only by manager
		else if (operation.equalsIgnoreCase("deleteevent")) {
		    torStub.LOG.info("---------Request recieved to delete the event by manager---------");
		    message = "";
		    message = torStub.removeEvent(eventType, eventId);
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For only listing availability of event from eventInfo only by manager
		else if (operation.equalsIgnoreCase("listeventmanager")) {
		    torStub.LOG.info("---------Request recieved to lsit the availability of the event by manager---------");
		    message = "";
		    for (Map.Entry<String, LinkedHashMap<String, Integer>> eventInfoEntry : torStub.eventInfo
			    .entrySet()) {
			if (eventInfoEntry.getKey().equalsIgnoreCase(eventType)) {
			    for (Entry<String, Integer> currentEvenEntry : eventInfoEntry.getValue().entrySet()) {
				message = message + "Event Id: " + currentEvenEntry.getKey() + "    Capacity: "
					+ currentEvenEntry.getValue() + " \n";
			    }
			}
		    }
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For booking event by customer only
		else if (operation.equalsIgnoreCase("bookevent")) {
		    torStub.LOG.info("---------Request recieved to book the new event---------");
		    message = "";
		    message = torStub.bookEvent(customerId, eventType, eventId);
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For getting booking schedule of perticular customer
		else if (operation.equalsIgnoreCase("getbookingschedule")) {
		    torStub.LOG.info("---------Request recieved to get the booking schedule of user---------");
		    message = "";
		    if (torStub.userInfo.containsKey(customerId)) {

			for (Map.Entry<String, ArrayList<String>> entry : torStub.userInfo.get(customerId).entrySet()) {
			    message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue()
				    + " \n";
			}
		    }
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}

//				For caceling event from perticular customer. Just increasing capacity of perticular event type and event id
		else if (operation.equalsIgnoreCase("cancelevent")) {
		    torStub.LOG.info("---------Request recieved to cancel the event---------");
		    message = "";
		    if (torStub.eventInfo.containsKey(eventType)) {
			if (torStub.eventInfo.get(eventType).containsKey(eventId)) {
			    int currentCapacity = torStub.eventInfo.get(eventType).get(eventId);
			    torStub.eventInfo.get(eventType).put(eventId, currentCapacity + 1);
			    message = "Event capacity has increased after cancelling event";
			} else
			    message = "Event not found in montreal server.";
		    }
		    torStub.LOG.info("Respond send by toronto server :"+message);
		}
//		For Checking if customer has booking for particular event or not
		else if (operation.equalsIgnoreCase("checkoldevent")) {
			torStub.LOG.info("---------Request recieved to check if old event is present or not---------");
			message = "";
			if (torStub.userInfo.containsKey(customerId)) {
				for (Map.Entry<String, ArrayList<String>> eventEntry : torStub.userInfo.get(customerId).entrySet()) {
					if (eventEntry.getValue().contains(oldEventId) && eventEntry.getKey().equalsIgnoreCase(oldEventType)) {
						message = "success";
						break;
					}
				}
			}
			torStub.LOG.info("Respond send by montreal server :" + message);
		}
//		For swapping user event
		else if (operation.equalsIgnoreCase("swapevent")) {
			torStub.LOG.info("---------Request recieved to swap event---------");
			message = "";
			message = torStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
			torStub.LOG.info("Respond send by toronto server :" + message);
		}
//		For swapping user event
		else if (operation.equalsIgnoreCase("checkifvalid")) {
			torStub.LOG.info("---------Request recieved to check for validity of event---------");
			message = "";
			if (torStub.eventInfo.containsKey(eventType)) {
				if (torStub.eventInfo.get(eventType).containsKey(eventId)) {
					if (torStub.eventInfo.get(eventType).get(eventId)>0) {
						message = "success";
					}
				}
			}
			
			torStub.LOG.info("Respond send by toronto server :" + message);
		}

		byte[] resultMessage = message.getBytes();
		reply = new DatagramPacket(resultMessage, message.length(), request.getAddress(), request.getPort());

		torStub.LOG.info("Result is: " + message);
		System.out.println(message);
		torStub.LOG.info("Sending reply////");
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
