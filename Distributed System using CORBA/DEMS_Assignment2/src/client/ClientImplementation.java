package client;


import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import IDL.IDLInterface;
import IDL.IDLInterfaceHelper;


/**
 * @author Yogesh Nimbhorkar
 *
 */
public class ClientImplementation {

    IDLInterface torStub, ottStub, montStub;
    String result;
    String initialID;
    String id;
    Logger logger = Logger.getLogger(ClientImplementation.class.getName());
    Scanner sc = new Scanner(System.in);
    String[] args;

    public ClientImplementation() {

	try {
		
		ORB orb = ORB.init(args, null);
		// -ORBInitialPort 1050 -ORBInitialHost localhost
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		torStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("torontoStub"));
		ottStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("ottawaStub"));
		montStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("montrealStub"));
		
	   
//	    testMulti();	//just to test multithreading at time of  demo

	}

	catch (Exception e) {
	    e.printStackTrace();

	}

    }

    /**
     * Main method to call all operatin based on role
     */
    public void systemHome() throws IOException {

	System.out.print("Welcome to Distributed Event Management System\n");
	System.out.println("Enter your ID");

	id = sc.next();

	id = id.toUpperCase();
	String role = verifyID(id);

	if (role.equalsIgnoreCase("manager")) {
	    initialID = id.substring(0, 3);
	    createLoggingFile(id);
	    managerOperations(id.toUpperCase());
	} else if (role.equalsIgnoreCase("user")) {
	    initialID = id.substring(0, 3);
	    createLoggingFile(id);
	    userOperations(id.toUpperCase());
	} else {
	    System.out.println("Enter a valid ID");
	    id = null;
	    System.out.println(
		    "Note: ID should me made of your city prefix i.e TOR, OTW or MTL followed by C or M for user or Manager respectively followed by four digit unique number.");
	    systemHome();
	}
	systemHome();
    }

    /**
     * All manager operations are handled in this method
     */
    public void managerOperations(String id) {
	logger.info("Manager operation has started with manager id " + id);
	String eventId, eventType, oldEventType, oldEventId = null;
	String custId;
	int preferredChoice = 0;
	System.out.print("Welcome to Manager Portal\n");
	System.out.println("Enter your Preferred choice\n");
	System.out.println("1. Add new event" + "\n" + "2. Remove an event" + "\n"
		+ "3. List the availability of event\n" + "4. Book new event for customer" + "\n"
		+ "5. Get booking schedule for customer" + "\n" + "6. Cancel event for customer"+ "\n" + "7. Swap event for customer" + "\n");

	if (sc.hasNextInt()) {
	    preferredChoice = sc.nextInt();
	} else {
	    System.out.println("Try again");
	    managerOperations(id);
	}

	sc.nextLine();
	switch (preferredChoice) {

	case 1:
	    logger.info("Manager selected option: 1. Add new event");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event id:  ");
	    eventId = validateEventId();
	    System.out.println("Enter capacity:  ");
	    int bookingCapacity = validateNumber();
	    sc.nextLine();
	    addnewEvent(eventType.toUpperCase(), eventId.toUpperCase(), bookingCapacity);
	    break;

	case 2:
	    logger.info("Manager selected option: 2. Remove an event");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter Event ID you wish to be removed\n");
	    eventId = validateEventId();
	    removeEvent(eventType.toUpperCase(), eventId.toUpperCase());
	    break;

	case 3:
	    logger.info("Manager selected option: 3. List the availability of event");
	    eventType = getEventTypeChoice();
	    listEventAvailability(eventType.toUpperCase());
	    break;

	case 4:
	    logger.info("Manager selected option: 4. Book new event for customer");
	    custId = getCustomerId();
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    bookNewEvent(custId.toUpperCase(), eventId.toUpperCase(), eventType.toUpperCase());
	    break;

	case 5:
	    logger.info("Manager selected option: 5. Get booking schedule for customer");
	    custId = getCustomerId();
	    getCustomerBookingSchedule(custId.toUpperCase());
	    break;

	case 6:
	    logger.info("Manager selected option: 6. Cancel event for customer");
	    custId = getCustomerId();
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    cancelEvent(custId.toUpperCase(), eventId.toUpperCase(), eventType.toUpperCase());
	    break;
	case 7:
	    logger.info("Manager selected option: 7. Swap event for customer");
	    custId = getCustomerId();
	    System.out.println("Enter choices for NEW EVENT\n");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    System.out.println("Enter choices for OLD EVENT\n");
	    oldEventType= getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    oldEventId = validateEventId();
	    swapevent(custId, eventId, eventType, oldEventType, oldEventId);
	    break;

	default:
	    logger.info("Manager has not chosen a valid option./nNeed to try again.");
	    System.out.println("Invalid Entry\n");
	    managerOperations(id.toUpperCase());
	    break;

	}

    }

    /**
     * Method to call differenct server implementation to add new event
     */
    public void addnewEvent(String eventType, String eventId, int eventCapacity) {
	logger.info("Manager Adding new event from system.");
	result = "";
	try {
	    if (initialID.equalsIgnoreCase("MTL") && eventId.substring(0, 3).equalsIgnoreCase("MTL")) {
		result = montStub.addEvent(eventId, eventType, eventCapacity);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("TOR") && eventId.substring(0, 3).equalsIgnoreCase("TOR")) {
		result = torStub.addEvent(eventId, eventType, eventCapacity);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("OTW") && eventId.substring(0, 3).equalsIgnoreCase("OTW")) {
		result = ottStub.addEvent(eventId, eventType, eventCapacity);
		System.out.println(result + "\n");
	    }
	    else
	    	System.out.println("Operation can not be performed.");

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method to remove event from system authorized by manager only.
     */
    public void removeEvent(String eventType, String eventId) {
	logger.info("Manager removing event from system.");
	result = "";
	try {
	    if (initialID.equalsIgnoreCase("MTL") && eventId.substring(0, 3).equalsIgnoreCase("MTL")) {
		result = montStub.removeEvent(eventType, eventId);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("TOR") && eventId.substring(0, 3).equalsIgnoreCase("TOR")) {
		result = torStub.removeEvent(eventType, eventId);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("OTW") && eventId.substring(0, 3).equalsIgnoreCase("OTW")) {
		result = ottStub.removeEvent(eventType, eventId);
		System.out.println(result + "\n");
	    }
	    else
	    	System.out.println("Operation can not be performed.");

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method to check the availability of events based on event type authorized by
     * manager
     */
    public void listEventAvailability(String eventType) {
	logger.info("Manager printing events availability from system.");
	result = "";
	try {
	    if (initialID.equalsIgnoreCase("MTL")) {
		result = montStub.listEventAvailability(eventType);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("TOR")) {
		result = torStub.listEventAvailability(eventType);
		System.out.println(result + "\n");
	    } else if (initialID.equalsIgnoreCase("OTW")) {
		result = ottStub.listEventAvailability(eventType);
		System.out.println(result + "\n");
	    }else
	    	System.out.println("Operation can not be performed.");

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method to handle all user realted operations.
     */
    public void userOperations(String id) throws IOException {

	System.out.println("Welcome to User Portal\n");
	int preferredChoice = 0;
	logger.info("User ID: " + id + " User is allowed to perform operations in this DEMS");
	System.out.println("Enter your Preferred choice\n");
	System.out.println("1. Book new event" + "\n" + "2. Get booking schedule" + "\n" + "3. Cancel event" + "\n"+ "4. Swap event" + "\n");

	if (sc.hasNextInt()) {
	    preferredChoice = sc.nextInt();
	} else {
	    System.out.println("Try again");
	    userOperations(id);
	}

	String eventId;
	String eventType;

	String oldEventType;
	String oldEventId;
	switch (preferredChoice) {
	case 1:
	    logger.info("User selected option: 1. Book new event");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    bookNewEvent(id.toUpperCase(), eventId.toUpperCase(), eventType.toUpperCase());
	    break;

	case 2:
	    logger.info("User selected option: 2. Get booking schedule");
	    getCustomerBookingSchedule(id.toUpperCase());
	    break;

	case 3:
	    logger.info("User selected option: 3. Cancel event");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    cancelEvent(id.toUpperCase(), eventId.toUpperCase(), eventType.toUpperCase());
	    break;
	    
	case 4:
	    logger.info("User selected option: 4. Swap event");
	    System.out.println("Enter choices for NEW EVENT\n");
	    eventType = getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    eventId = validateEventId();
	    System.out.println("Enter choices for OLD EVENT\n");
	    oldEventType= getEventTypeChoice();
	    System.out.println("Enter event ID:  ");
	    oldEventId = validateEventId();
	    swapevent(id.toUpperCase(), eventId.toUpperCase(), eventType.toUpperCase(), oldEventType, oldEventId);
	    break;

	default:
	    System.out.println("Invalid Entry");
	    logger.info("User ID: " + id + " User has not chosen a valid option./nNeed to try again.");
	    userOperations(id);
	    break;

	}
    }

    /**
     * Method to call server implementation to book new event
     */
    public void bookNewEvent(String userId, String eventId, String eventType) {
	logger.info("User booking new event in the system");
	result = "";
	try {
	    if ((userId.substring(0, 3)).equalsIgnoreCase("MTL")) {
		result = montStub.bookEvent(userId, eventType, eventId);
		System.out.println(result + "\n");
	    } else if ((userId.substring(0, 3)).equalsIgnoreCase("TOR")) {
		result = torStub.bookEvent(userId, eventType, eventId);
		System.out.println(result + "\n");
	    } else if ((userId.substring(0, 3)).equalsIgnoreCase("OTW")) {
		result = ottStub.bookEvent(userId, eventType, eventId);
		System.out.println(result + "\n");
	    }

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method calls server implementation get booking of customer
     */
    public void getCustomerBookingSchedule(String customerId) {
	logger.info("User getting schedule for booked events from the system");
	result = "";
	try {
	    if ((customerId.substring(0, 3)).equalsIgnoreCase("MTL")) {
		result = montStub.getBookingSchedule(customerId);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("TOR")) {
		result = torStub.getBookingSchedule(customerId);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("OTW")) {
		result = ottStub.getBookingSchedule(customerId);
		System.out.println(result + "\n");
	    }

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method calls server implementation get booking of customer
     */
    public void cancelEvent(String customerId, String eventId, String eventType) {
	logger.info("User cancelling booking of the event from the system");
	result = "";
	try {
	    if ((customerId.substring(0, 3)).equalsIgnoreCase("MTL")) {
		result = montStub.cancelEvent(customerId, eventType, eventId);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("TOR")) {
		result = torStub.cancelEvent(customerId, eventType, eventId);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("OTW")) {
		result = ottStub.cancelEvent(customerId, eventType, eventId);
		System.out.println(result + "\n");
	    }

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }
    
    /**
     * Method calls server implementation swap event of customer
     */
    public void swapevent(String customerId, String eventId, String eventType, String oldEventType, String oldEventId) {
	logger.info("User cancelling booking of the event from the system");
	result = "";
	try {
	    if ((customerId.substring(0, 3)).equalsIgnoreCase("MTL")) {
		result = montStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("TOR")) {
		result = torStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
		System.out.println(result + "\n");
	    } else if ((customerId.substring(0, 3)).equalsIgnoreCase("OTW")) {
		result = ottStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
		System.out.println(result + "\n");
	    }

	    validateChoice();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    /**
     * Method to validate event id
     */
    public String validateEventId() {
	String choice = sc.nextLine().toUpperCase();

	Pattern r;
	String s;

	r = Pattern.compile("(MTL|TOR|OTW)[MAE][0-9]{6}");

	Matcher M = r.matcher(choice);

	if (M.matches()) {
	    return choice;
	} else {
	    System.out.println(
		    "Enter Valid ID. Event ID must consist of initial three letters of (TOR/OTW/MTL) any city followed by unique four digits(0-9).");
	    System.out.println("Enter it again");
	    s = validateEventId();
	}
	return s;

    }

    /**
     * Method to validate if user / manager continue to use or exit.
     */
    public void validateChoice() throws IOException {
	System.out.println("Do you want to Continue. Press Y/N to continue or end");

	String choice = sc.next().toUpperCase();
	Pattern regex = Pattern.compile("(Y|N)");
	Matcher match = regex.matcher(choice);

	if (match.matches()) {
	    if (choice.toUpperCase().equalsIgnoreCase("Y")) {
		String role = verifyID(id);
		if (role.equalsIgnoreCase("manager")) {
		    managerOperations(id.toUpperCase());
		} else
		    userOperations(id.toUpperCase());
	    } else
		systemHome();
	} else {
	    System.out.println("Enter valid choice");
	    validateChoice();
	}
    }

    /**
     * Method to validate the capacity
     */
    public int validateNumber() {

	int number;
	do {
	    System.out.println("Please enter any positive number. ");
	    while (!sc.hasNextInt()) {
		String input = sc.next();
		System.out.println(input + " is not a valid number. Number should be positive\n");
	    }
	    number = sc.nextInt();
	} while (number < 0);

	return number;
    }

    /**
     * Method to validate id of manager or user
     */
    public String verifyID(String id) throws IOException {
	if (id == null) {
	    System.out.println(
		    "ID cannot be null. It should be constructed from the acronym of their branch’s city i.e TOR, OTW or MTL followed by C or M for user or Manager respectively followed by four digit unique number.");
	    systemHome();
	}

	String choice = id;
	choice = choice.toUpperCase();
	Pattern regex = Pattern.compile("(TOR|OTW|MTL)([CM])[0-9]{4}");
	Matcher match = regex.matcher(choice);

	if (match.matches()) {
	    char ch = id.charAt(3);

	    if (ch == 'C') {
		result = "user";
	    } else {
		result = "manager";
	    }

	} else {
	    result = "invalid user";
	}
	return result;
    }

    /**
     * Method to get id of users when manager is performing user's operations
     */
    private String getCustomerId() {
	String returnId = null;
	System.out.println("Enter customer ID");
	String tempId = sc.next();
	tempId = tempId.toUpperCase();
	Pattern regex = Pattern.compile("(TOR|OTW|MTL)([CM])[0-9]{4}");
	Matcher match = regex.matcher(tempId);

	if (match.matches()) {
	    returnId = tempId;
	} else {
	    System.out.println("Enter valid choice");
	    getCustomerId();
	}

	return returnId;
    }

    /**
     * Method to create log file for user and manager.
     */
    public void createLoggingFile(String id) {
	File file = null;
	FileHandler handler = null;
	LogManager.getLogManager().reset();

	if (id.charAt(3) == 'C') {
	    file = new File("C:\\Users\\yoges\\Desktop\\Distributed system\\User\\logs");
	} else {
	    file = new File("C:\\Users\\yoges\\Desktop\\Distributed system\\Manager\\logs");
	}

	if (!file.exists()) {
	    file.mkdirs();
	}

	File f = new File(file.getAbsolutePath().toString() + "\\" + id + ".txt");
	try {
	    if (!f.exists()) {
		f.createNewFile();
	    }

	    handler = new FileHandler(f.getAbsolutePath(), true);

	    handler.setFormatter(new SimpleFormatter());
	    handler.setLevel(Level.INFO);
	    logger.addHandler(handler);
	} catch (SecurityException e) {

	    e.printStackTrace();
	} catch (IOException i) {
	    i.printStackTrace();
	}

    }

    /**
     * Method to select which type of event to select
     */
    private String getEventTypeChoice() {
	int selectedOption = 0;
	System.out.println("Enter your Preferred choice\n");
	System.out.println("1. Coference" + "\n" + "2. Seminar" + "\n" + "3. Trade show" + "\n");
	if (sc.hasNextInt()) {
	    selectedOption = sc.nextInt();
	} else {
	    System.out.println("Try again");
	    getEventTypeChoice();
	}

	sc.nextLine();
	String choice = null;
	if (selectedOption == 1) {
	    choice = "CONFERENCES";
	} else if (selectedOption == 2) {
	    choice = "SEMINARS";
	} else if (selectedOption == 3) {
	    choice = "TRADE SHOWS";
	}

	return choice;
    }

}
