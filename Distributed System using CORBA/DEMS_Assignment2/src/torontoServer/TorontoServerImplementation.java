package torontoServer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.omg.CORBA.ORB;

import IDL.IDLInterfacePOA;

public class TorontoServerImplementation extends IDLInterfacePOA {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public LinkedHashMap<String, LinkedHashMap<String, Integer>> eventInfo = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();

    public LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> userInfo = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>();

    int dummyCapacity = 2;

    public Logger LOG = Logger.getLogger(TorontoServerImplementation.class.getName());
    int torontoPort = 5555;
    int ottawaPort = 6666;
    int montrealPort = 7777;
    private ORB orb;
    String message = "";

    public String getCurrentDate() {

	Date date = new Date();

	SimpleDateFormat sdf = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
	return sdf.format(date);

    }

    public void createLoggingFile(String system) {
	File file = null;
	FileHandler handler = null;
	LogManager.getLogManager().reset();

	file = new File("C:\\Users\\yoges\\Desktop\\Distributed system\\Server\\logs");

	if (!file.exists()) {
	    file.mkdirs();
	}

	File f = new File(file.getAbsolutePath().toString() + "\\" + system + ".txt");
	try {
	    if (!f.exists()) {
		f.createNewFile();
	    }

	    handler = new FileHandler(f.getAbsolutePath(), true);

	    handler.setFormatter(new SimpleFormatter());
	    handler.setLevel(Level.INFO);
	    LOG.addHandler(handler);
	} catch (SecurityException | IOException e) {

	    e.printStackTrace();
	}

    }

    public TorontoServerImplementation() {

	createLoggingFile("Toronto");

	eventInfo.put("CONFERENCES", new LinkedHashMap<String, Integer>());
	eventInfo.get("CONFERENCES").put("TORA100519", dummyCapacity);
	eventInfo.get("CONFERENCES").put("TORE100519", dummyCapacity);

	eventInfo.put("SEMINARS", new LinkedHashMap<String, Integer>());
	eventInfo.get("SEMINARS").put("TORA100519", dummyCapacity);
	eventInfo.get("SEMINARS").put("TORE100519", dummyCapacity);

	eventInfo.put("TRADE SHOWS", new LinkedHashMap<String, Integer>());
	eventInfo.get("TRADE SHOWS").put("TORA100519", dummyCapacity);
	eventInfo.get("TRADE SHOWS").put("TORE100519", dummyCapacity);

	userInfo.put("TORC2345", new LinkedHashMap<String, ArrayList<String>>());
	userInfo.get("TORC2345").put("CONFERENCES", new ArrayList<String>() {
	    {
		add("TORA100519");
	    }
	});
	userInfo.get("TORC2345").put("SEMINARS", new ArrayList<String>() {
	    {
		add("TORA100519");
	    }
	});
	
	

    }

    @Override
    public synchronized String addEvent(String eventId, String eventType, int bookingCapacity) {

	message = "";
	String eventPrefix = eventId.substring(0, 3);
	String reply1;

	LOG.info("The current Date and time for this request is: " + getCurrentDate());

	if ((eventId.subSequence(0, 3).toString()).equalsIgnoreCase("TOR")) {

	    if (eventInfo.containsKey(eventType)) {

		if (eventInfo.get(eventType).containsKey(eventId)) {

		    eventInfo.get(eventType).put(eventId, bookingCapacity);

		    message = "Event with Event ID:  " + eventId + "  has been successfully updated in the DEMS";
		    LOG.info("SUCCESS");

		} else {
		    eventInfo.get(eventType).put(eventId, bookingCapacity);
		    message = "New event with event type: " + eventType + " and event id as " + eventId
			    + "  has been successfully added in the system" + "\n";

		}

	    } else {
		eventInfo.put(eventType, new LinkedHashMap<String, Integer>());
		eventInfo.get(eventType).put(eventId, bookingCapacity);
		message = "Event with Event ID:  " + eventType + "  has been successfully added in the system";
		LOG.info("SUCCESS");
	    }
	    LOG.info(message);

	} else if (eventPrefix.equalsIgnoreCase("MTL")) {
	    reply1 = send("addeventmanager", eventType, eventId, montrealPort, null, bookingCapacity, null, null);
	    message = message + reply1;

	} else if (eventPrefix.equalsIgnoreCase("OTW")) {
	    reply1 = send("addeventmanager", eventType, eventId, ottawaPort, null, bookingCapacity, null, null);
	    message = message + reply1;
	} else
	    message = "Event can not be added into the system.";

	return message;

    }

    @Override
    public synchronized String removeEvent(String eventType, String eventId) {

	message = "";
	String reply1;
	String reply2;
	boolean deletionFlag = false;
	String eventPrefix = eventId.substring(0, 3);

	if (eventPrefix.equalsIgnoreCase("TOR")) {

	    if (eventInfo.containsKey(eventType)) {

		if (eventInfo.get(eventType).containsKey(eventId)) {

//					Removing event from eventInfo map of Montreal
		    eventInfo.get(eventType).remove(eventId);
//					Removing from userInfo map of montreal
		    for (String user : userInfo.keySet()) {
			if (userInfo.get(user).containsKey(eventType)) {
			    if (userInfo.get(user).get(eventType).contains(eventId)) {
				userInfo.get(user).get(eventType).remove(eventId);
			    }
			}
		    }
//					Removing from other servers userInfo map
		    reply1 = send("removeuserevent", eventType, eventId, montrealPort, null, 0, null, null);
		    reply2 = send("removeuserevent", eventType, eventId, ottawaPort, null, 0, null, null);
		    message = reply1 + "\n" + reply2;
		    message = "Successfully removed EventID. All associated users have been dealt with.";
		    deletionFlag = true;
		}
	    }
	    if (!deletionFlag) {
		message = "event id: " + eventId + " does not exist in the system so cannot perform deletion";
	    }

	} else if (eventPrefix.equalsIgnoreCase("MTL")) {

	    reply1 = send("deleteevent", eventType, eventId, montrealPort, null, 0, null, null);
	    message = reply1;

	} else if (eventPrefix.equalsIgnoreCase("OTW")) {

	    reply1 = send("deleteevent", eventType, eventId, ottawaPort, null, 0, null, null);

	    message = reply1;
	} else
	    message = "Event removal failed as no match found.";

	return message;
    }

    @Override
    public synchronized String listEventAvailability(String eventType) {

	message = "";
	String reply;

	message = "Below is the list of all the available events in the System: \n";
	LOG.info("The current Date and time for this request is: " + getCurrentDate());
//		Printing all Montreal server events availability for perticular type
	message = message + "Event type: " + eventType + "\n";
	for (String typeOfEvent : eventInfo.keySet()) {
	    if (typeOfEvent.equalsIgnoreCase(eventType)) {
		for (String idOfEvent : eventInfo.get(typeOfEvent).keySet()) {
		    message = message + "Event Id: " + idOfEvent + "    Capacity: "
			    + eventInfo.get(typeOfEvent).get(idOfEvent) + "\n";
		}
	    }
	}
//		Printing other servers event availability using UDP 
	for (String typeOfEvent : eventInfo.keySet()) {
	    reply = send("listeventmanager", typeOfEvent, null, montrealPort, null, 0, null, null);
	    message = message + reply + "\n";
	}
	for (String typeOfEvent : eventInfo.keySet()) {
	    reply = send("listeventmanager", typeOfEvent, null, ottawaPort, null, 0, null, null);
	    message = message + reply + "\n";
	}

	LOG.info("--SUCCESS--");

	LOG.info(message);

	return message;
    }

    @Override
    public synchronized String bookEvent(String customerId, String eventType, String eventId) {

	String reply1;
	String eventPrefix = eventId.substring(0, 3);
	boolean successFlag = false;
	message = "";

	LOG.info("The current Date and time for this request is: " + getCurrentDate());

	if (eventPrefix.equalsIgnoreCase("TOR")) {
	    boolean duplicateEventFlag = false;
	    if (userInfo.containsKey(customerId)) {
		if (userInfo.get(customerId).containsKey(eventType)) {
		    if (userInfo.get(customerId).get(eventType).contains(eventId)) {
			duplicateEventFlag = true;
			message = "User already has been regstered for the event. User can not book same event twice.";
			LOG.info("User already has been regstered for the event. User can not book same event twice.");

		    }
		}
	    }
	    if (!duplicateEventFlag) {

		if (eventInfo.containsKey(eventType)) {
		    if (eventInfo.get(eventType).containsKey(eventId)) {
			if (eventInfo.get(eventType).get(eventId) > 0) {

			    successFlag = true;
			    message = "success/n";
			    int currentCapacity = eventInfo.get(eventType).get(eventId);
//					Decremented capacity by one
			    eventInfo.get(eventType).put(eventId, currentCapacity - 1);

			    message = "Event ID: " + eventId + " has been successfully issued to the user with user ID: "
				    + customerId + ";\n";
			    LOG.info("----SUCCESS----");
			    LOG.info(message);

//					return message;
			} else {
			    message = "Event is full";
			    LOG.info("Event is full");
//					return message;
			}
		    } else {
			message = "Event does not exist in the server";
			LOG.info("Event not available.");
//					return message;
		    }
		} else {
		    message = "Event does not exist in the server";
		    LOG.info("Event not available.");
//				return message;
		}

	    }
	}

	else if (eventPrefix.equalsIgnoreCase("MTL")) {
	    if (checkUserBookingLimit(customerId, eventId)) {
		reply1 = send("bookevent", eventType, eventId, montrealPort, customerId, 0, null, null);
		message = reply1;
		if (reply1.contains("success")) {
		    successFlag = true;
		}

	    } else {
	    	if (message.length()<5) {
	    		message = "Customer can book at most 3 events from other cities overall in a month.";
			}
	    }
	} else if (eventPrefix.equalsIgnoreCase("OTW")) {
	    if (checkUserBookingLimit(customerId, eventId)) {
		reply1 = send("bookevent", eventType, eventId, ottawaPort, customerId, 0, null, null);
		message = reply1;
		if (reply1.contains("success")) {
		    successFlag = true;
		}
	    } else {
	    	if (message.length()<5) {
	    		message = "Customer can book at most 3 events from other cities overall in a month.";
			}
	    }
	} else
	    message = "book event operation failed as no match found. Please try again.";

	if (successFlag) {
//			Updating userInfo map
	    if (userInfo.containsKey(customerId)) {
		if (userInfo.get(customerId).containsKey(eventType)) {
		    userInfo.get(customerId).get(eventType).add(eventId);
		} else {
		    userInfo.get(customerId).put(eventType, new ArrayList<String>() {
			{
			    add(eventId);
			}
		    });
		}
	    } else {
		userInfo.put(customerId, new LinkedHashMap<String, ArrayList<String>>());
		userInfo.get(customerId).put(eventType, new ArrayList<String>() {
		    {
			add(eventId);
		    }
		});
	    }
	}

	LOG.info("result is: " + message);
	LOG.info("Sending result back to the user.");
	return message;
    }

    /**
     * @param customerId
     * @param eventId
     * @return
     */
    private boolean checkUserBookingLimit(String customerId, String eventId) {
	boolean result = true;
	ArrayList<String> currentUserEvents = new ArrayList<String>();
	String checkDate = eventId.substring(6, 8);
	int limitCounter = 0;

	if (userInfo.containsKey(customerId)) {
	    for (Map.Entry<String, LinkedHashMap<String, ArrayList<String>>> userEntry : userInfo.entrySet()) {
		for (Map.Entry<String, ArrayList<String>> eventTypeEntry : userEntry.getValue().entrySet()) {
		    currentUserEvents.addAll(eventTypeEntry.getValue());
		}
	    }
	}

	for (String currEvent : currentUserEvents) {
	    if (currEvent.substring(6, 8).equalsIgnoreCase(checkDate)
		    && !(currEvent.substring(0, 3).equalsIgnoreCase("TOR"))) {
		limitCounter++;
	    }
	}
	if (limitCounter >= 3) {
	    result = false;
	}

	return result;
    }

    @Override
    public synchronized String getBookingSchedule(String customerId) {

	message = "";
	if (userInfo.containsKey(customerId)) {
	    for (Map.Entry<String, ArrayList<String>> entry : userInfo.get(customerId).entrySet()) {
		message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue() + " \n";
	    }
	}

	if (message.length() == 0 || message == null) {
	    message = "No booking found for user " + customerId;
	}
	return message;
    }

    @Override
    public synchronized String cancelEvent(String customerId, String eventType, String eventId) {

	String eventPrefix = eventId.substring(0, 3);

	message = "";
	String reply1;
	boolean foundFlag = false;

//		Removing event record from userInfo map if available
	if (userInfo.containsKey(customerId)) {
	    for (Map.Entry<String, ArrayList<String>> eventEntry : userInfo.get(customerId).entrySet()) {
		if (eventEntry.getValue().contains(eventId) && eventEntry.getKey().equalsIgnoreCase(eventType)) {
		    eventEntry.getValue().remove(eventId);
		    foundFlag = true;
		    break;
		}
	    }
	}

	if (foundFlag) {
	    if (eventPrefix.equalsIgnoreCase("TOR")) {
//			Finding capacity for perticular event and increasing by one
		for (Map.Entry<String, LinkedHashMap<String, Integer>> eventTypeEntry : eventInfo.entrySet()) {
		    if (eventTypeEntry.getKey().equalsIgnoreCase(eventType)) {
			for (Map.Entry<String, Integer> eventEntry : eventTypeEntry.getValue().entrySet()) {
			    if (eventEntry.getKey().equalsIgnoreCase(eventId)) {
				int currentCapacity = eventEntry.getValue();
				eventEntry.setValue(currentCapacity + 1);
				message = "Event has been canceled and capacity increased by 1. \n";
			    }
			}
		    }
		}
	    } else if (eventPrefix.equalsIgnoreCase("MTL")) {

		reply1 = send("cancelevent", eventType, eventId, montrealPort, customerId, 0, null, null);
		message = reply1;

	    } else if (eventPrefix.equalsIgnoreCase("OTW")) {

		reply1 = send("cancelevent", eventType, eventId, ottawaPort, customerId, 0, null, null);

		message = reply1;
	    }
	} else {
	    LOG.info("Event does not exist for provided customerId");
	    message = "Event does not exist for provided customerId";
	}
	LOG.info("The Result is: ");
	LOG.info(message);
	LOG.info("Sending reply back to the client (User) ");

	return message;

    }

	/* 
	 * Swapping old event with new event
	 */
	@Override
	public synchronized String swapEvent(String customerId, String newEventId, String newEventType, String oldEventId, String oldEventType) {
		
		String eventPrefix = newEventId.substring(0, 3);

		message = "";
		String reply1;
		boolean foundFlag = true;



		if (foundFlag) {
		    if (eventPrefix.equalsIgnoreCase("TOR")) {
//			cancel old event//		    book new event
		    if (checkOldEventBooking(customerId, oldEventType, oldEventId) && checkNewEventBooking(customerId, newEventType, newEventId)) {
		    	message=message+cancelEvent(customerId, oldEventType, oldEventId);
		    	message=message+bookEvent(customerId, newEventType, newEventId);
				message=message+" Event swapped successfully";
			}	
		    	
		    } else if (eventPrefix.equalsIgnoreCase("MTL")) {

			reply1 = send("swapevent", newEventType, newEventId, montrealPort, customerId, 0, oldEventType, oldEventId);
			message = message + "\n" + reply1;

		    } else if (eventPrefix.equalsIgnoreCase("OTW")) {

			reply1 = send("swapevent",  newEventType, newEventId, ottawaPort, customerId, 0, oldEventType, oldEventId);

			message = message + "\n" + reply1;
		    }
		} else {
		    LOG.info("Event swapping can't be done.");
		    message = "Event swapping can't be done.";
		}
		LOG.info("The Result is: ");
		LOG.info(message);
		LOG.info("Sending reply back to the client (User) ");

		return message;

	    }

	String send(String key, String eventType, String eventId, int serverPort, String userID, int capacity, String oldEventType, String oldEventId) {
    LOG.info("Toronto server sending request  "+key+" with Event Type: "+eventType+" Event Id: "+eventId+" Server Port: "+serverPort+" User Id: "+userID+" Capacity: "+capacity+ " Old Event Type: " + oldEventType + " Old Event Id: " + oldEventId);
	DatagramSocket aSocket = null;
	String messageReceived = null;
	try {

		 key = key + "," + eventType + "," + eventId + "," + serverPort + "," + userID + "," + capacity+ "," + oldEventType + "," + oldEventId;
	    aSocket = new DatagramSocket();
	    byte[] mess = key.getBytes();
	    InetAddress aHost = InetAddress.getByName("localhost");
	    DatagramPacket request = new DatagramPacket(mess, key.length(), aHost, serverPort);
	    aSocket.send(request);
	    byte[] buffer = new byte[1000];
	    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

	    aSocket.receive(reply);

	    messageReceived = new String(reply.getData());
	    messageReceived = messageReceived.trim();

	} catch (SocketException e) {
	    System.out.println("Socket: " + e.getMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    System.out.println("IO: " + e.getMessage());
	} finally {
	    if (aSocket != null)
		aSocket.close();
	}
	LOG.info("Toronto server got the respose back "+messageReceived);
	return messageReceived;

    }

	/**
	 * Check if new booking is possible for given customerId 
	 * 
	 * @param customerId
	 * @param newEventType
	 * @param newEventId
	 * @return
	 */
	private boolean checkNewEventBooking(String customerId, String newEventType, String newEventId) {
		boolean result=false;
		boolean duplicate=true;
		boolean validEvent=false;
		boolean validLimit=true;
		String newEventPrefix=newEventId.substring(0,3);
		String reply1="";

//		Check if new event is valid and its capacity not zero
		if (newEventPrefix.equalsIgnoreCase("TOR")) {
			if (eventInfo.containsKey(newEventType)) {
				if (eventInfo.get(newEventType).containsKey(newEventId)) {
					if (eventInfo.get(newEventType).get(newEventId)>0) {
						validEvent=true;
					}
				}
			}
		 } else if (newEventPrefix.equalsIgnoreCase("MTL")) {
				reply1 = send("checkifvalid", newEventType, newEventId, montrealPort, customerId, 0,  "null", "null");

	    } else if (newEventPrefix.equalsIgnoreCase("OTW")) {
				reply1 = send("checkifvalid", newEventType, newEventId, ottawaPort, customerId, 0,  "null", "null");
			    }
			
			if (reply1.contains("success")) {
				validEvent= true;
			}
		
//		Check for duplicate event
		if (userInfo.containsKey(customerId)) {
			if (userInfo.get(customerId).containsKey(newEventType)) {
				if (userInfo.get(customerId).get(newEventType).contains(newEventId)) {
					duplicate = false;
				} 
			}
		}
		
//		Check if event limit not reached
		if (!newEventPrefix.equalsIgnoreCase("TOR")) {
			validLimit=checkUserBookingLimit(customerId, newEventId);
		}
		
		result=(duplicate && validEvent && validLimit);
		if (!result) {
			message= message + " Please validate new event information/n";
		}
		return result;
	}

	/**
	 * Checking if customer has booking for particular event or not
	 *  
	 * @param customerId
	 * @param oldEventType
	 * @param oldEventId
	 * @return
	 */
	private synchronized boolean checkOldEventBooking(String customerId, String oldEventType, String oldEventId) {
		boolean result=false;
		

//		Checking if customerId having booking for oldEventId and type
		if (userInfo.containsKey(customerId)) {
			for (Map.Entry<String, ArrayList<String>> eventEntry : userInfo.get(customerId).entrySet()) {
				if (eventEntry.getValue().contains(oldEventId) && eventEntry.getKey().equalsIgnoreCase(oldEventType)) {
					result = true;
					break;
				}
				else message="customer don't have event booking for event Id "+ oldEventId+"\n";
			}
		}
		
	 
		return result;
	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

}
