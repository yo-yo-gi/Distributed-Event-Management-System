
package serverandclient;
import java.util.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
/*
Whenever there is a need of UDP, create an object of UDPRequest and spawn a thread of it to get the result
 */

import java.net.*;
import java.text.SimpleDateFormat;


import org.omg.CORBA.ORB;

import customerBookingApp.*;

public class ServerImpl{

	private String nameOfCity;
    private HashMap<String,Integer> serverPortMap;
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> database = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();
    private Logger logger;
		private ORB orb;
		UDPServer UDPServerInstance;
		
		public static Thread serverThread=null;
		 private volatile static boolean running;
		 static DatagramSocket aSocket = null;
		
		
		public void setORB(ORB orb_val) {
			orb = orb_val;
		}

		public void shutdown() {
			orb.shutdown(false);
		}
	
	    ServerImpl(String city, int udpPort) {
	    	
	    	this.nameOfCity = city;
	    	this.UDPServerInstance = new UDPServer(this,udpPort);
	    	
	    	
	        this.serverPortMap = new HashMap<String, Integer>();
	        serverPortMap.put("MTL",6660);
	        serverPortMap.put("TOR",7770);
	        serverPortMap.put("OTW",8880);
	        
	        //this.rmiPort=rmiPort;
	        this.database = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();

	        //Initialize the logger
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
	        System.setProperty("input_file_name", ".//serverLogs//"+nameOfCity+ "_"+ dateFormat.format(new Date()));
	        this.logger = LogManager.getLogger(ServerImpl.class);
	        PropertyConfigurator.configure("G:\\Final_Project\\Final_Project\\log4j_ds.properties");
	        
	    }

	    public String getNameOfCity() {
	        return this.nameOfCity;
	    }

	    //@Override
	    public synchronized String addEvent(String eventID, String eventType, String capacity) {
	//Complete
	        String eType, eID;
	        int eCapacity;
	        eType = eventType;
	        eID = eventID;
	        eCapacity = Integer.parseInt(capacity);

	        if (!eventID.substring(0, 3).equals(this.nameOfCity)) {
	            System.out.println("addEvent: Error! " + eventID + " is not for " + this.nameOfCity + " server");
	            this.logger.error("addEvent: Error! " + eventID + " is not for " + this.nameOfCity + " server");
	            //return "5";
	            return "FAILURE| addEvent: Error! " + eventID + " is not for " + this.nameOfCity + " server";
	        }

	        if (eCapacity <= 0) {
	            System.out.println("addEvent: Error! invalid value of capacity!");
	            this.logger.error("addEvent: Error! invalid value of capacity!");
	            //return "5";
	            return "FAILURE| addEvent: Error! invalid value of capacity!";
	        }
	        
	        HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	        HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>();

	        //Check if the database has an event type added
	        if (this.database.containsKey(eType)) {
	            //if(this.database.containsValue(eID)){
	            outerMap = this.database.get(eType);
	            //Check if the database already has the event added then, just modify the capacity values.
	            if (outerMap.containsKey(eID)) {
	                innerMap = outerMap.get(eID);
	                ArrayList<String> aL = new ArrayList<String>();
	                aL = innerMap.get("Capacity");
	                aL.set(0, Integer.toString(eCapacity));//Modified the existing available capacity
	                //Replacing the modified value of capacity
	                if (innerMap.replace("Capacity", innerMap.get("Capacity"), aL)) { //Converting the int to string and modifying the values. It will return true/false
	                    //!missing - Add the logs in server file to indicate that the capacity value for an event has been updated.
	                    System.out.println("addEvent: Event capacity has been modified ");
	                    this.logger.info("addEvent: Event capacity has been modified for "+eID);
	                    //return "0";
	                    return "SUCCESS| addEvent: Event capacity has been modified for "+eID;
	                }
	            } else {
	                ArrayList<String> aL = new ArrayList<String>();
	                aL.add(Integer.toString(eCapacity));
	                ArrayList<String> cust = new ArrayList<String>();
	                innerMap.put("Capacity", aL); //Capacity and customer array are the inner most attribute
	                innerMap.put("Customers", cust);

	                outerMap.put(eID, innerMap);
	                this.database.replace(eType, outerMap);
	                System.out.println("addEvent: Success! Event type exists. New event "+eventID+" has been added.");
	                this.logger.info("addEvent: Success! Event type exists. New event "+eventID+" has been added.");
	                //return "1";
	                return "SUCCESS| addEvent: Success! Event type exists. New event "+eventID+" has been added.";
	            }

	        } else {
	            //Simply add the new event
	            //    HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	            //  HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>(); //Hashmap to store inner information for a eventy type key
	            ArrayList<String> aL = new ArrayList<String>();
	            aL.add(Integer.toString(eCapacity));
	            ArrayList<String> cust = new ArrayList<String>();
	            innerMap.put("Capacity", aL); //Capacity is the inner most attribute, we can add more attributes based on the requirements
	            innerMap.put("Customers", cust);

	            outerMap.put(eID, innerMap);
	            this.database.put(eType, outerMap);
	            System.out.println("addEvent: New event "+eventID+" has been added.");
	            this.logger.info("addEvent: New event "+eventID+" has been added.");
	            //return "1";
	            return "SUCCESS| addEvent: New event "+eventID+" has been added.";
	        }
	        System.out.println("addEvent: Error! "+eventID +" could not be added.");
	        this.logger.error("addEvent: Error! "+eventID +" could not be added.");
	        //return "2"; //incase event manager was unable  add the event
	        return "FAILURE| addEvent: Error! "+eventID +" could not be added.";
	    }

	    //@Override
	    public synchronized String removeEvent(String eID, String eType) {
	        //Complete

	        if (!eID.substring(0, 3).equals(this.nameOfCity)) {
	            System.out.println("removeEvent: Error! " + eID + " is not for " + this.nameOfCity + " server");
	            this.logger.error("removeEvent: Error! " + eID + " is not for " + this.nameOfCity + " server");
	            //return "5";
	            return "FAILURE| removeEvent: Error! " + eID + " is not for " + this.nameOfCity + " server";
	        }

	        if (this.database.containsKey(eType)) {
	            HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	            outerMap = this.database.get(eType);
	            if (outerMap == null) {
	                System.out.println("removeEvent: Error! No event exist under " + eType);
	                this.logger.error("removeEvent: Error! No event exist under " + eType);
	                //return "2";
	                return "FAILURE| removeEvent: Error! No event exist under " + eType;
	            }
	            for (Map.Entry mIN : outerMap.entrySet()) {
	                if (mIN.getKey().equals(eID)) {
	                    outerMap.remove(mIN.getKey()); //Removing the event
	                    System.out.println("removeEvent: Event " + eID + " has been removed");
	                    this.logger.info("removeEvent: Event " + eID + " has been removed");
	                    //return "1";
	                    return "SUCCESS| removeEvent: Event " + eID + " has been removed";
	                }
	                //System.out.println("Such event does not exist "+" "+"Event name "+eID);
	            }
	            
                System.out.println("removeEvent: Error! " + eID + " does not exist under " + eType);
                this.logger.error("removeEvent: Error! " + eID + " does not exist under " + eType);
                //return "2";// for unable to find an event
                return "FAILURE| removeEvent: Error! " + eID + " does not exist under " + eType; 

	        }
	        System.out.println("removeEvent - Error! Could not remove the event " + eID + " Event id under " + eType + " Event type catagory");
	        this.logger.error("removeEvent - Error! Could not remove the event " + eID + " Event id under " + eType + " Event type catagory");
	        //return "3";
	        return "FAILURE| removeEvent - Error! Could not remove the event " + eID + " Event id under " + eType + " Event type catagory";
	    }

	    //@Override
	    public synchronized String listEventAvailability(String eventType) {
	        //Get the event list under this server
	        String eventsOnThisServer = innerlistEventAvailability(eventType);
	        System.out.println("Now get the events list from other servers");
	        HashMap temp = new HashMap<String, Integer>();
	        temp = this.serverPortMap;
	        temp.remove(this.nameOfCity);
	        Set s = new HashSet();
	        Collection c = temp.values();
	        Iterator itr = c.iterator();
	        int tport1 = (Integer)itr.next();
	        int tport2 =(Integer)itr.next();
	        String inputParams = eventType; //Here input params is eventType only; it may change for other methods
	        String methodName = "innerlistEventAvailability";
	        String responseFromUDP1 = callUDPtoServers(methodName,inputParams,tport1);
	        String responseFromUDP2 = callUDPtoServers(methodName,inputParams,tport2);

	        return "SUCCESS| " + eventsOnThisServer + responseFromUDP1 + responseFromUDP2;
	    }

	    private String callUDPtoServers(String methodName, String inputParams, int port1){
	        //Get the iterator for serverPortMap
	        /*ArrayList<String> serverList = new ArrayList<>();
	        for (Map.Entry e : serverPortMap.entrySet()) {
	            //If this server then skip
	            if (e.getKey().equals(this.nameOfCity)) {
	                continue;
	            }
	            serverList.add((String)e.getKey()); //This will contain all the servers except the current server
	        }*/

	        //Take the first server and perform UDP
	        String resp1="";
	        //String resp2="";
	        int tgtPort1 = port1;//(Integer) this.serverPortMap.get(serverList.get(0));//This is target port for the first server
	        System.out.println("First target port is "+tgtPort1);
	        String methodName1 = methodName;

	        try {
	            InetAddress tgtServer1 = InetAddress.getByName("localhost");
	            //String inputParams1 = eventType;
	            String inputParams1 = inputParams;
	            String sendMessage1 = methodName1 + "&" + inputParams1;
	            System.out.println("VALUE OF sendMessgage is "+sendMessage1);
	            UDPRequest request1 = new UDPRequest(tgtServer1, tgtPort1, sendMessage1); //This will create an instance of UDPRequest
	            Thread req1 = new Thread(request1);
	            req1.run();

	            resp1 = request1.getResponseVal(); //Got the response from the thread
	            System.out.println("Value of first response is "+resp1);

	            try {

	                req1.join(); //Wait till the above(req1) thread terminates
	   //             req2.join(); //Wait till the above(req2) thread terminates
	            } catch (InterruptedException ex) {
	                ex.printStackTrace();
	            }

//	            System.out.println("Value of second response is "+resp2);
	        }
	        catch (UnknownHostException ex) {
	            ex.printStackTrace();
	        }

	    //return resp1 + resp2; //Return retrieved response from both the servers
	        return resp1; //Return retrieved response from both the servers
	    }

	    public synchronized String innerlistEventAvailability(String eventType) {

	        HashMap<String, Integer> availableEvents = new HashMap<String, Integer>();
	        //String output=eventType + " -";
	        String output = "";
	        HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	        HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>();

	        System.out.println("Value of eventType is "+eventType);
	        outerMap = this.database.get(eventType);

	        if ((outerMap != null) && (!outerMap.isEmpty())) {

	            for (Map.Entry m : outerMap.entrySet()) {
	                innerMap = outerMap.get(m.getKey());

	                ArrayList<String> aL = new ArrayList<String>();
	                aL = innerMap.get("Capacity");
	                availableEvents.put((String) m.getKey(), Integer.parseInt(aL.get(0)));//
	            }

	            for (Map.Entry m : availableEvents.entrySet()) {
	                output = output + " " + m.getKey() + " " + m.getValue() + "#";
	            }

	        }
	        //output=output.substring(0,output.length()-1);
	        System.out.println("listEventaAvailability: Executed successfully for " + eventType);
	        this.logger.info("listEventaAvailability: Executed successfully for " + eventType);
	        return output;//Returning the available events
	    }

	    //@Override
	    public synchronized String bookEvent(String custID, String eID, String eType) {

	    	//System.out.println("VALUES RECEIVED --- "+ custID + "--"+ eID + "--"+ eType+"--");
	        //Check if the customer belong to same location
	        if(!custID.substring(0,3).equals(nameOfCity)){
	            System.out.println("bookEvent: Error! customer "+custID+" does not belong to "+this.nameOfCity);
	            //return "7";
	            return "FAILURE| bookEvent: Error! customer "+custID+" does not belong to "+this.nameOfCity;
	        }
/*
	        if(custID.substring(3,4).equals("C") && !eID.substring(0,3).equals(nameOfCity)){
	            System.out.println("bookEvent: Error! customer "+custID+" can not book event "+ eID +", belong to "+eID.substring(0,3) + " server.");
	            return "8";
	        }	*/        
	        
	        //Check if the customer has exhausted the limit on number of bookings in a month
	        //Trigger getBookingSchedule only on the server where request has arrived
	        //Event is on the same server hence directly book the event
	        if (eID.substring(0, 3).equals(this.nameOfCity)){
	            System.out.println("calling  the innerbookEvent for this server ");
	            String returnFrominnerBookevent = innerbookEvent(custID,eID,eType);
	            System.out.println("Returning from innerbookevent "+returnFrominnerBookevent);
	            return returnFrominnerBookevent;
	        }

	        System.out.println("Now get the booking details from other servers");
	        HashMap temp = new HashMap<String, Integer>();
	        temp = this.serverPortMap;
	        temp.remove(this.nameOfCity);
	        Collection c = temp.values();
	        Iterator itr = c.iterator();
	        int tport1 = (Integer)itr.next();
	        int tport2 = (Integer)itr.next();
	        System.out.println("Calling on "+tport1 + " "+tport2);
	        String inputParams = custID; //Here input params is eventType only; it may change for other methods
	        String methodName = "innergetBookingSchedule";
	        String responseFromUDP1 = callUDPtoServers(methodName,inputParams,tport1);
	        String responseFromUDP2 = callUDPtoServers(methodName,inputParams,tport2);

	        System.out.println("Returning the list for getbooking details :" + responseFromUDP1 + responseFromUDP2);

	            String bookingDetails = responseFromUDP1 + responseFromUDP2;
	            System.out.println("Return value of getBookingschedule - " + bookingDetails);
	///*
	            //There is at least one booking present
	            if(!bookingDetails.equals("")) {
	                System.out.println("There is at least one booking present");
	                String[] bookedEvent = bookingDetails.split(":");
	                //Find the number of events booked in that month
	                int noofeventsforthismonth = 0;
	                String monthForthisEvent = eID.substring(6,eID.length());
	                for (int t = 0; t < bookedEvent.length; t++) {
	                    if(bookedEvent[t].substring(6,bookedEvent[t].length()).equals(monthForthisEvent)){
	                        //bookedEvent[t] has same month as of monthForthisEvent
	                        noofeventsforthismonth +=1;
	                    }
	                }
	                if(noofeventsforthismonth >= 3){
	                    //Reached the capacity of booking maximum 3 event per month
	                    System.out.println("bookEvent: Error! "+custID+" has reached the maximum limit for "+monthForthisEvent + " month");
	                    this.logger.error("bookEvent: Error! "+custID+" has reached the maximum limit for "+monthForthisEvent + " month");
	                    //return "5";
	                    return "FAILURE| bookEvent: Error! "+custID+" has reached the maximum limit for "+monthForthisEvent + " month";
	                }
	            }
	                System.out.println("Going to book the event on another server");
	                tport1 = this.serverPortMap.get(eID.substring(0, 3));
	                methodName = "innerbookEvent";
	                inputParams = custID + "," + eID + "," + eType;
	                System.out.println("Calling innerbookevent with " + inputParams + " parameters & "+tport1 + " port");
	                responseFromUDP1 = callUDPtoServers(methodName, inputParams, tport1);
	                System.out.println("Received value from UDPResponse " + responseFromUDP1);
	                //int res = Integer.parseInt(responseFromUDP1.trim()); //Changed
	                String res = responseFromUDP1.trim();
	                return res;
	    }

	    public synchronized String innerbookEvent(String custID, String eID, String eType) {

	        System.out.println("Inside innerbook event");
	            if (this.database.containsKey(eType)) {
	                HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	                outerMap = this.database.get(eType);
	                if (outerMap.containsKey(eID)) {
	                    HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>();
	                    innerMap = outerMap.get(eID);
	                    if (innerMap.get("Customers").contains(custID)) {
	                        System.out.println("bookEvent: " + custID + " has already booked " + eID + " event");
	                        this.logger.info("bookEvent: " + custID + " has already booked " + eID + " event");
	                        //return "3";
	                        return "SUCCESS| bookEvent: " + custID + " has already booked " + eID + " event";
	                    }
	                    if (Integer.parseInt(innerMap.get("Capacity").get(0)) > 0) { //Here, for capacity there will only be 1 element and hence accessing index 0
	                        //Seat is available hence reduce the available seats and book one seat
	                        ArrayList<String> t = innerMap.get("Capacity");
	                        String t1 = Integer.toString(Integer.parseInt(t.get(0)) - 1);
	                        t.set(0, t1); //New capacity
	                        ArrayList<String> custs = innerMap.get("Customers");
	                        custs.add(custID); //New customer list
	                        if (innerMap.replace("Customers", custs) != null && innerMap.replace("Capacity", t) != null) { //Add new customer ID and reduce the available seats;
	                            if (outerMap.replace(eID, innerMap) != null) {
	                                //Seats are modified and customer has been added.
	                                System.out.println("bookEvent: " + custID + " customer was able to book the " + eID + " event " + "on "+this.nameOfCity + " server");
	                                this.logger.info("bookEvent: " + custID + " customer was able to book the " + eID + " event " + "on "+this.nameOfCity + " server");
	                                //return "1";
	                                return "SUCCESS| bookEvent: " + custID + " customer was able to book the " + eID + " event " + "on "+this.nameOfCity + " server";
	                            }
	                        }
	                    } else
	                        {
	                        System.out.println("bookEvent: " + custID + " customer could not book the " + eID + " event as seats are unavailable at this moment!");
	                        this.logger.error("bookEvent: " + custID + " customer could not book the " + eID + " event as seats are unavailable at this moment!");
	                        //return "3";
	                        return "FAILURE| bookEvent: " + custID + " customer could not book the " + eID + " event as seats are unavailable at this moment!";
	                        }
	                }
	            } else {
	                System.out.println("Event ID does not match");
	                logger.info("Event ID does not match");
	                //return "2";
	                return "FAILURE| Event ID does not match";
	            }
	        //}
	        System.out.println("innerbookEvent: ERROR!! " + custID + " customer could not book the " + eID + " event");
	        this.logger.error("innerbookEvent: ERROR!! " + custID + " customer could not book the " + eID + " event");
	        //return "4";
	        return "FAILURE| innerbookEvent: ERROR!! " + custID + " customer could not book the " + eID + " event";
	    }

	    //@Override
	    //public List<String> getBookingSchedule(String customerID) {
	    public synchronized String getBookingSchedule(String customerID) {
	//Complete

	        String outputFromThisServer = this.innergetBookingSchedule(customerID);

	        //Call other servers if the caller belong to same server
	        HashMap<String, Integer> temp = new HashMap<String, Integer>();
	        temp = this.serverPortMap;
	        temp.remove(this.nameOfCity);
	        Collection c = temp.values();
	        Iterator<Integer> itr = c.iterator();
	        int tport1 = (Integer)itr.next();
	        int tport2 = (Integer)itr.next();
	        System.out.println("Calling on "+tport1 + " "+tport2);
	        String inputParams = customerID; //Here input params is eventType only; it may change for other methods
	        String methodName = "innergetBookingSchedule";
	        String responseFromUDP1 = callUDPtoServers(methodName,inputParams,tport1);
	        String responseFromUDP2 = callUDPtoServers(methodName,inputParams,tport2);

	        System.out.println("Returning the list for getbooking details :" + outputFromThisServer + responseFromUDP1 + responseFromUDP2);
	        //return outputFromThisServer + responseFromUDP1 + responseFromUDP2;
	        return "SUCCESS|" + outputFromThisServer.concat(responseFromUDP1.concat(responseFromUDP2)) ;
	    }

	    public synchronized String innergetBookingSchedule(String customerID) {
	//Complete
	        ArrayList<String> eventList = new ArrayList<String>();
	        HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	        HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>();

	        for (Map.Entry m : this.database.entrySet()) {
	            outerMap = (HashMap<String, HashMap<String, ArrayList<String>>>) m.getValue();
	            for (Map.Entry m1 : outerMap.entrySet()) {
	                innerMap = (HashMap<String, ArrayList<String>>) m1.getValue();
	                if (innerMap.get("Customers").contains(customerID)) {
	                    //eventList.add(m1.getKey());//Adding this event to the list as customer is part of this event
	                	String eventtypeandname = (String)m1.getKey() +" ("+ (String)m.getKey() + ")";
	                    eventList.add(eventtypeandname);//Adding this event to the list as customer is part of this event
	                    System.out.println("getBooking schedule for " + customerID);
	                }
	            }
	        }
	        //Adding new code to covert arrayList to string in order to pass the details
	        String sendEventList = ""; //Will send empty string incase the eventList is empty
	        if (!eventList.isEmpty()) {
	            //If event list is not empty then append the list elements to form a string
	            System.out.println("Event list is not empty " + eventList.size());
	            for (int i = 0; i < eventList.size(); i++) {
	                sendEventList = sendEventList + eventList.get(i)+ ":";//Sending pipe separated data which will be recovered on the client side
	            }
	        }
	        this.logger.info("innergetBookingSchedule executed for "+customerID);
	          return sendEventList;
	    }

	    //@Override
	    public synchronized String cancelEvent(String custID, String eID, String eType) {
	        //Complete
	        if (eID.substring(0, 3).equals(this.nameOfCity)) {// hence event can be removed from the same database
	            //HashMap<String, HashMap<String, ArrayList<String>>> outerMap = new HashMap<String, HashMap<String, ArrayList<String>>>();
	            //outerMap = (HashMap<String, HashMap<String, ArrayList<String>>>) this.database.get(eType); //Get the outerMap corresponding to the Event type
	        	HashMap<String, HashMap<String, ArrayList<String>>> outerMap = (HashMap<String, HashMap<String, ArrayList<String>>>) this.database.get(eType); //Get the outerMap corresponding to the Event type
	        	
	        	//	          String outerEType = "";

	            //for (Map.Entry m : this.database.entrySet()) {
	                //Find the presense of event in the database
	                ///////  HashMap<String, HashMap<String, List<String>>> outerM= new HashMap<String, HashMap<String, List<String>>>();
	                //outerMap = (HashMap<String, HashMap<String, ArrayList<String>>>) m.getValue();
	                //outerEType = (String) m.getKey();

	                boolean ifFound = false;
	                for (Map.Entry mIN : outerMap.entrySet()) {
	                    System.out.println("added key is " + mIN);
	                    //System.out.println("Value of GETKEY is "+mIN.getKey() + " eID is "+eID);
	                    if (mIN.getKey().equals(eID)) {
	                        ifFound = true;
	                        //System.out.println("key found !!!");
	                        break;
	                    }
	                }

	                //if (ifFound) {
	                  //  System.out.println("It should break the loop");
	                    //break;
	                //} else {
	                if(!ifFound){
	                    System.out.println("cancelEvent: such event does not exist " + "Requested by " + custID + " " + " Event name " + eID);
	                    this.logger.error("cancelEvent: such event does not exist " + "Requested by " + custID + " " + " Event name " + eID);
	                    //return "2";//1 for unable to find an event
	                    return "FAILURE| cancelEvent: such event does not exist " + "Requested by " + custID + " " + " Event name " + eID;
	                }

	            //}
	            HashMap<String, ArrayList<String>> innerMap = new HashMap<String, ArrayList<String>>();
	//Get the key which has the eID value
	/*
	                String eType=null;
	                for(Map.Entry m: this.database.entrySet()){
	                    if(m.getValue()==eID){
	                        eType=(String)m.getKey(); //Event type is the key for a EventID
	                        break;
	                    }
	                }
	*/
	            //outerMap = this.database.get(outerEType); //Not needed?

	            boolean idFound = false;
	            for (Map.Entry<String, HashMap<String, ArrayList<String>>> m2 : outerMap.entrySet()) {
	                if (m2.getKey().equals(eID)) idFound = true;
	            }
	            if (!idFound) {
	                System.out.println("cancelEvent: Error!! event id and event type mismatch");
	                this.logger.error("cancelEvent: Error!! event id and event type mismatch");
	                //return "3";
	                return "FAILURE| cancelEvent: Error!! event id and event type mismatch";
	            }

	            innerMap = outerMap.get(eID);

	            ArrayList<String> aL, t;// = new ArrayList<String>();
	            aL = innerMap.get("Customers");
	            t = innerMap.get("Capacity");

	            String t1 = t.get(0);
	            t1 = Integer.toString(Integer.parseInt(t1) + 1);
	            //System.out.println("I am here!1");
//	                Start from here
	            if (aL.remove(custID)) {      //Removed the customer ID from the available customer IDs
	                t.set(0, t1);            //Modified the available seat
	                innerMap.replace("Capacity", t); //Replace the count with the modified value
	                innerMap.replace("Customers", aL); //Replace the new customer list without the current customer ID
	                //Update the database;
	                outerMap.replace(eID, innerMap);
	                //this.database.replace(outerEType, outerMap);
	                this.database.replace(eType, outerMap);
	                //Successfully canceled the Event
	                System.out.println("cancelEvent: event " + eID + " canceled by " + custID);
	                this.logger.info("cancelEvent: event " + eID + " canceled by " + custID);
	                //return "1";
	                return "cancelEvent: event " + eID + " canceled by " + custID;
	            }
	            //}
	        }
	        //else{//Invoke the methods on other server based on the server location

	        else { //Event does not exist on the same server and hence do the UDP
	            int tport1 = this.serverPortMap.get(eID.substring(0, 3));
	            String methodName = "cancelEvent";
	            String inputParams = custID +","+ eID +","+ eType;
	            System.out.println("Calling cancelEvent with "+inputParams+" parameters ");
	            String responseFromUDP1 = callUDPtoServers(methodName,inputParams,tport1);
	            System.out.println("Received value from UDPResponse "+responseFromUDP1);
	            //int res =  Integer.parseInt(responseFromUDP1.trim());
	            String res =  responseFromUDP1.trim();
	            return res;
	        }
	        System.out.println("cancelEvent: Error! Could not cancel event due to technical issue");
	        this.logger.error("cancelEvent: Error! Could not cancel event due to technical issue");
	        //return "3"; //Could not cancel event due to technical issue
	        return "cancelEvent: Error! Could not cancel event due to technical issue";
	    }

	    //@Override
	    public synchronized String swapEvent(String custID, String newEventID, String newEventType, String oldEventID, String oldEventType ){
	        //Check if the old and new event type is same else return
	      /*  if(!newEventType.equals(oldEventType)){
	            System.out.println("swapEvent: Error! event type does not match for "+custID+". swapEvent requires the types to be same");
	            this.logger.error("swapEvent: Error! event type does not match for "+custID+". swapEvent requires the types to be same");
	            return "5";
	        }
	        */
	        //Get the old booked event list from another server
	        if(getBookingSchedule(custID).contains(oldEventID)) //This confirms that customer has booked oldEventID
	        {
	            //Book new event
	            //int newReturn = this.bookEvent(custID, newEventID, newEventType);
	            String newReturn = this.bookEvent(custID, newEventID, newEventType);
	                //if(!newReturn.equals("1")){
	            if(newReturn.substring(0,newReturn.indexOf('|')).toLowerCase().equals("failure")){
	                System.out.println("swapEvent: Error! "+custID+" could not book new event "+newEventID+" hence old event "+oldEventID + " can not be cancelled");
	                this.logger.error("swapEvent: Error! "+custID+" could not book new event "+newEventID+" hence old event "+oldEventID + " can not be cancelled");
	                //return "2";
	                return "FAILURE| swapEvent: Error! "+custID+" could not book new event "+newEventID+" hence old event "+oldEventID + " can not be cancelled";
	            }
	            //Cancel old event
	            //int oldReturn = this.cancelEvent(custID, oldEventID, oldEventType);
	            String oldReturn = this.cancelEvent(custID, oldEventID, oldEventType);

	                //if(!oldReturn.equals("1")){
	            if(newReturn.substring(0,newReturn.indexOf('|')).toLowerCase().equals("failure")){
	                System.out.println("swapEvent: Error! "+custID+" could not cancel old event");
	                this.logger.error("swapEvent: Error! "+custID+" could not cancel old event");
	                //If cancel fails then shall I cancel the new booked event?
	                //return "2";
	                return "FAILURE| swapEvent: Error! "+custID+" could not cancel old event";
	            }
	            System.out.println("swapEvent: SUCCESS! event was swapped for "+custID +" new event is "+newEventID +" under "+newEventType);
	            this.logger.info("swapEvent: SUCCESS! event was swapped for "+custID +" new event is "+newEventID +" under "+newEventType);
	            //return "1";
	            return "SUCCESS| swapEvent: SUCCESS! event was swapped for "+custID +" new event is "+newEventID +" under "+newEventType;
	        }
	        System.out.println("swapEvent: Error! event "+oldEventID +" is not booked by customer "+custID);
	        this.logger.error("swapEvent: Error! event "+oldEventID +" is not booked by customer "+custID);
	        //return "3";
	        return "FAILURE| swapEvent: Error! event "+oldEventID +" is not booked by customer "+custID;
	    }
}