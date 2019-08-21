package torontoServer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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

import interfaceRMI.RMIInterface;

public class TorontoServerImplementation extends UnicastRemoteObject implements RMIInterface {

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

    public TorontoServerImplementation() throws RemoteException {

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
		add("TORE100519");
	    }
	});
	userInfo.get("TORC2345").put("SEMINARS", new ArrayList<String>() {
	    {
		add("TORE100519");
	    }
	});
	userInfo.get("TORC2345").put("TRADE SHOWS", new ArrayList<String>() {
	    {
		add("MTLE100519");
	    }
	});

    }

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#addEvent(java.lang.String, java.lang.String,
     * java.lang.String, int)
     */
    @Override
    public synchronized String addEvent(String eventId, String eventType, int bookingCapacity) throws RemoteException {

	String message = "";
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
		message = "Item with Item ID:  " + eventType + "  has been successfully added in the system";
		LOG.info("SUCCESS");
	    }
	    LOG.info(message);

	} else if (eventPrefix.equalsIgnoreCase("MTL")) {
	    reply1 = send("addeventmanager", eventType, eventId, montrealPort, null, bookingCapacity);
	    message = message + reply1;

	} else if (eventPrefix.equalsIgnoreCase("OTW")) {
	    reply1 = send("addeventmanager", eventType, eventId, ottawaPort, null, bookingCapacity);
	    message = message + reply1;
	} else
	    message = "Event can not be added into the system.";

	return message;

    }

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#removeEvent(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public synchronized String removeEvent(String eventType, String eventId) throws RemoteException {

	String message = "";
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
		    reply1 = send("removeuserevent", eventType, eventId, montrealPort, null, 0);
		    reply2 = send("removeuserevent", eventType, eventId, ottawaPort, null, 0);
		    message = reply1 + "\n" + reply2;
		    message = "Successfully removed itemID. All associated users have been dealt with.";
		    deletionFlag = true;
		}
	    }
	    if (!deletionFlag) {
		message = "event id: " + eventId + " does not exist in the system so cannot perform deletion";
	    }

	} else if (eventPrefix.equalsIgnoreCase("MTL")) {

	    reply1 = send("deleteevent", eventType, eventId, montrealPort, null, 0);
	    message = reply1;

	} else if (eventPrefix.equalsIgnoreCase("OTW")) {

	    reply1 = send("deleteevent", eventType, eventId, ottawaPort, null, 0);

	    message = reply1;
	} else
	    message = "Event removal failed as no match found.";

	return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#listEventAvailability(java.lang.String)
     */
    @Override
    public synchronized String listEventAvailability(String eventType) throws RemoteException {

	String message = "";
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
	    reply = send("listeventmanager", typeOfEvent, null, montrealPort, null, 0);
	    message = message + reply + "\n";
	}
	for (String typeOfEvent : eventInfo.keySet()) {
	    reply = send("listeventmanager", typeOfEvent, null, ottawaPort, null, 0);
	    message = message + reply + "\n";
	}

	LOG.info("--SUCCESS--");

	LOG.info(message);

	return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#bookEvent(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public synchronized String bookEvent(String customerId, String eventType, String eventId) throws RemoteException {

	String reply1;
	String eventPrefix = eventId.substring(0, 3);
	boolean successFlag = false;
	String message = "";

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

			    message = "Item ID: " + eventId + " has been successfully issued to the user with user ID: "
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
		reply1 = send("bookevent", eventType, eventId, montrealPort, customerId, 0);
		message = reply1;
		if (reply1.contains("success")) {
		    successFlag = true;
		}

	    } else {
		message = "Customer can book at most 3 events from other cities overall in a month.";
	    }
	} else if (eventPrefix.equalsIgnoreCase("OTW")) {
	    if (checkUserBookingLimit(customerId, eventId)) {
		reply1 = send("bookevent", eventType, eventId, ottawaPort, customerId, 0);
		message = reply1;
		if (reply1.contains("success")) {
		    successFlag = true;
		}
	    } else {
		message = "Customer can book at most 3 events from other cities overall in a month.";
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
	boolean result = false;
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
	if (limitCounter <= 3) {
	    result = true;
	}

	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#getBookingSchedule(java.lang.String)
     */
    @Override
    public synchronized String getBookingSchedule(String customerId) throws RemoteException {

	String message = "";
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

    /*
     * (non-Javadoc)
     * 
     * @see interfaceRMI.RMIInterface#cancelEvent(java.lang.String,
     * java.lang.String)
     */
    @Override
    public synchronized String cancelEvent(String customerId, String eventType, String eventId) throws RemoteException {

	String eventPrefix = eventId.substring(0, 3);

	String message = "";
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
				message = "Event has been cancled and capacity increased by 1. ";
			    }
			}
		    }
		}
	    } else if (eventPrefix.equalsIgnoreCase("MTL")) {

		reply1 = send("cancelevent", eventType, eventId, montrealPort, customerId, 0);
		message = reply1;

	    } else if (eventPrefix.equalsIgnoreCase("OTW")) {

		reply1 = send("cancelevent", eventType, eventId, ottawaPort, customerId, 0);

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

    String send(String key, String eventType, String eventId, int serverPort, String userID, int capacity) {
	LOG.info("Toronto server sending request  "+key+" with Event Type: "+eventType+" Event Id: "+eventId+" Server Port: "+serverPort+" User Id: "+userID+" Capacity: "+capacity);
	DatagramSocket aSocket = null;
	String messageReceived = null;
	try {

	    key = key + "," + eventType + "," + eventId + "," + serverPort + "," + userID + "," + capacity;
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

}
