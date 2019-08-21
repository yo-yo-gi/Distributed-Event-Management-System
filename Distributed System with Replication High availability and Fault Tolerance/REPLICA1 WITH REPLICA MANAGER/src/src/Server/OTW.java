package src.Server;


import src.Client.Logger;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


public class OTW implements ServerInterface{

		 protected LinkedHashMap<String, LinkedHashMap<String, Integer>> Events;
		    protected LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> Users;
		    protected Logger Serverlog;
		    protected String Server;
		    protected String TargetServer;
		    protected int TargetPort;
		    protected int UDPPort;
		    protected String OtherServers;
		    private volatile static boolean running;
		    private static Thread thread = null;
		    
		    private static DatagramSocket aSocket = null;

		public OTW() throws Exception{
	       // super();
	        Events = new LinkedHashMap<String, LinkedHashMap<String, Integer>> ();
	        Users = new LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>>();
	        Server = "OTW";
	        OtherServers = "TOR,MTL";
	        Serverlog = new Logger("OTWServer.log");
	        Serverlog.WriteLog(" OTW Server has started");
	        UDPPort = 5555;
	        Runnable task = () -> {
	            try {
	                receive();
	            }
	            catch (Exception e){

	            }
	        } ;
	         thread = new Thread(task);
	        thread.start();
	    }
		public static void stopServer() {
			 if (thread == null) return;
	         running = false;
	         aSocket.close();
		}
	    public synchronized String BookEvent(String CustID, String EventID, String EventType) throws Exception{
	        String message = "";
	        String Event = EventID.substring(0, 3);
	        String Month = EventID.substring(6, 8);
	        boolean status = false;
	        int CurrentCapacity = 0;
	        boolean duplicate = false;

	        if (Event.equalsIgnoreCase(Server)) {
	            if (Users.containsKey(CustID)) {
	                if (Users.get(CustID).containsKey(EventType)) {
	                    if (Users.get(CustID).get(EventType).contains(EventID)) {
	                        duplicate = true;
	                        message = "User already booked for the event " + "EventID: " + EventID + "EventType: " + EventType + " User can not book same event twice.";
	                    }
	                }
	            }
	            if (!duplicate) {
	                if (Events.containsKey(EventType)) {
	                    if (Events.get(EventType).containsKey(EventID)) {
	                        if (Events.get(EventType).get(EventID) > 0) {
	                            CurrentCapacity = Events.get(EventType).get(EventID);
	                            Events.get(EventType).put(EventID, CurrentCapacity - 1);
	                            status = true;
	                            message = "EventID: " + EventID + "EventType: " + EventType + " has been successfully booked by user: " + CustID + "\n";
	                        } else {
	                            message = "No seats available for event EventID: " + EventID + "EventType: " + EventType ;
	                        }
	                    } else {
	                        message = "EventID does not exist EventID: " + EventID + "EventType: " + EventType;
	                    }
	                } else {
	                    message = "EventType does not exist EventID: " + EventID + "EventType: " + EventType;
	                }
	            }
	            if (status) {
	                if (Users.containsKey(CustID)) {
	                    if (Users.get(CustID).containsKey(EventType)) {
	                        Users.get(CustID).get(EventType).add(EventID);
	                    } else {
	                    	ArrayList<String> a =  new ArrayList<String>();
	                    	a.add(EventID);
	                    	Users.get(CustID).put(EventType, a);
	                     /*   Users.get(CustID).put(EventType, new ArrayList<String>() {
	                            {
	                                add(EventID);
	                            }
	                        });*/
	                    }
	                } else {
	                    Users.put(CustID, new LinkedHashMap<String, ArrayList<String>>());
	                    Users.get(CustID).put(EventType, new ArrayList<String>() {
	                        {
	                            add(EventID);
	                        }
	                    });
	                }
	            }
	        }
	        else {
	            int Othercnt = 0;
	            String[] NumServers = OtherServers.split(",");
	            for(int cnt = 0;cnt<NumServers.length;cnt ++) {
	                SetUDP(NumServers[cnt]);
	                Othercnt = Othercnt + Integer.parseInt(sendUDPMessage("GetBookingCount", TargetPort, Month, "", CustID, 0,"",""));
	            }
	            Serverlog.WriteLog(" Booking count from other servers " + Othercnt);
	            if(Othercnt > 2){
	                message = " Booked more than 3 events from other servers, cannot book";
	                Serverlog.WriteLog(" Booked more than 3 events from other servers, cannot book");
	                Othercnt = 0;
	            }
	            else {
	                SetUDP(Event);
	                message = sendUDPMessage("BookEvent", TargetPort, EventType, EventID, CustID, 0,"","");
	                if (message.substring(0, message.indexOf("|")).trim().equals("Success")) {
	                    message = message.substring(message.indexOf("|") + 1);
	                    status = true;
	                } else
	                    message = message.substring(message.indexOf("|") + 1);
	            }
	        }
	        Serverlog.WriteLog(" Request Type: Book Event " +
	                " Request " +
	                "Parameters: ClientID - " + CustID +
	                " EventID - " + EventID +
	                " EventType - " + EventType +
	                " Status - " + status);

	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Failure|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    public synchronized String GetBookingSchedule(String CustID, boolean Internal) throws Exception{
	        String message = "";
	        String temp = "";
	        String Event = CustID.substring(0, 3);
	        boolean status = false;
	        if (Event.equalsIgnoreCase(Server)) {
	            if (Users.containsKey(CustID)) {
	                for (Map.Entry<String, ArrayList<String>> entry : Users.get(CustID).entrySet()) {
	                    if(entry.getValue().size() > 0) {
	                        message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue() + " \n";
	                        status = true;
	                    }
	                }
	            }
	        }

	        if(!Internal) {
	            String[] NumServers = OtherServers.split(",");
	            for(int cnt = 0;cnt<NumServers.length;cnt ++) {
	                SetUDP(NumServers[cnt]);
	                temp = sendUDPMessage("GetBookingSchedule", TargetPort, "", "", CustID, 0,"","");
	                if (temp.substring(0, temp.indexOf("|")).trim().equals("Success")) {
	                    temp = temp.substring(temp.indexOf("|") + 1);
	                    status = true;
	                    message = message + " " + temp;
	                }
	            }
	        }
	        else{
	            if (Users.containsKey(CustID)) {
	                for (Map.Entry<String, ArrayList<String>> entry : Users.get(CustID).entrySet()) {
	                    if(entry.getValue().size() > 0) {
	                        message = message + " Event type:" + entry.getKey() + " Event id:" + entry.getValue() + " \n";
	                        status = true;
	                    }
	                }
	            }
	        }
	        if (message.length() == 0 || message == null || message == "") {
	            Serverlog.WriteLog("No booking found for user " + CustID);
	            message = "No booking found for user " + CustID;
	        }
	        Serverlog.WriteLog(" Request Type: GetBookingSchedule " +
	                " Request Parameters: ClientID - " + CustID +
	                " Status - " + status);

	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Success|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    public synchronized String CancelEvent(String CustID, String EventID,String EventType) throws Exception{
	        String Event = EventID.substring(0, 3);
	        String message = "";
	        String response = "";
	        boolean found = false;
	        boolean status = false;

	        if (Event.equalsIgnoreCase(Server)) {
	            if (Users.containsKey(CustID)) {
	                for (Map.Entry<String, ArrayList<String>> eventEntry : Users.get(CustID).entrySet()) {
	                    if (eventEntry.getValue().contains(EventID) && eventEntry.getKey().equalsIgnoreCase(EventType)) {
	                        eventEntry.getValue().remove(EventID);
	                        found = true;
	                        break;
	                    }
	                }
	            }
	            if (found) {
	                if (Event.equalsIgnoreCase(Server)) {
	                    for (Map.Entry<String, LinkedHashMap<String, Integer>> eventTypeEntry : Events.entrySet()) {
	                        if (eventTypeEntry.getKey().equalsIgnoreCase(EventType)) {
	                            for (Map.Entry<String, Integer> eventEntry : eventTypeEntry.getValue().entrySet()) {
	                                if (eventEntry.getKey().equalsIgnoreCase(EventID))
	                                {
	                                    eventEntry.setValue(eventEntry.getValue() + 1);
	                                    message = "EventType: " + EventType + " EventID: " + EventID +  " has been successfully cancelled";
	                                    status = true;
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	            else
	                message = "EventType: " + EventType + " EventID: " + EventID +  " is not booked by user";

	        }
	        else{
	            SetUDP(Event);
	            message = sendUDPMessage("CancelEvent",TargetPort,EventType,EventID,CustID,0,"","");
	            if (message.substring(0, message.indexOf("|")).trim().equals("Success")) {
	                message = message.substring(message.indexOf("|") + 1);
	                status = true;
	            } else
	                message = message.substring(message.indexOf("|") + 1);
	        }
	        Serverlog.WriteLog(" Request Type: CancelEvent " +
	                " Request Parameters: ClientID - " + CustID +
	                " EventID - " + EventID +
	                " EventType - " + EventType +
	                " Status - " + status);

	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Failure|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    public synchronized String AddEvent(String CustID, String EventID, String EventType, int Capacity) throws Exception {
	        //Serverlog.UserID = CustID;
	        String message = "";
	        Boolean status = false;

	        if (EventID.substring(0, 3).equalsIgnoreCase(Server)) {
	            if (Events.containsKey(EventType)) {
	                if (Events.get(EventType).containsKey(EventID)) {
	                    Events.get(EventType).put(EventID, Capacity);
	                    message = "EventID: " + EventID + "  has been successfully updated with capacity: "+ Capacity;
	                    status = true;
	                }
	                else {
	                    Events.get(EventType).put(EventID, Capacity);
	                    message = "EventID has been successfully added, EventType: " + EventType +
	                              " EventID: " + EventID +
	                              " Capacity: " + Capacity;
	                    status = true;
	                }
	            }
	            else {
	                Events.put(EventType, new LinkedHashMap<String, Integer>());
	                Events.get(EventType).put(EventID, Capacity);
	                message = "EventType has been successfully added, EventType: " + EventType +
	                          " EventID: " + EventID +
	                          " Capacity: " + Capacity;
	                status = true;
	            }
	        }
	        else
	        {
	            message = "EventType cannot be added/update - incorrect details, EventType: " + EventType +
	                    " EventID: " + EventID +
	                    " Capacity: " + Capacity;
	            status = false;
	        }

	        Serverlog.WriteLog(" Request Type: AddEvent " +
	                " Request Parameters: ClientID - " + CustID +
	                " EventID - " + EventID +
	                " EventType - " + EventType +
	                " Capacity - " + Capacity +
	                " Status - " + status);

	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Failure|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    public synchronized String RemoveEvent(String EventID, String EventType) throws Exception{
	        String message = "";
	        String reply1;
	        String reply2;
	        boolean status = false;
	        boolean deletionFlag = false;
	        String eventPrefix = EventID.substring(0, 3);

	        if (eventPrefix.equalsIgnoreCase(Server)) {
	            if (Events.containsKey(EventType)) {
	                if (Events.get(EventType).containsKey(EventID)) {
	                    Events.get(EventType).remove(EventID);
	                    status = true;
	                    message = "Event removed successfully , EventID: "+ EventID +" EventType: "+ EventType ;
	                    for (String User : Users.keySet()) {
	                        if (Users.get(User).containsKey(EventType)) {
	                            if (Users.get(User).get(EventType).contains(EventID)) {
	                                Users.get(User).get(EventType).remove(EventID);
	                                Users.get(User).remove(EventType);
	                            }
	                        }
	                    }
	                }
	            }
	            if (!status) {
	                message = "Unable to remove event, EventID: "+ EventID +" EventType: "+ EventType + " doesn't not exist.";
	            }

	        }
	        else
	            message = "Event removal failed as no match found.";
	        Serverlog.WriteLog(" Request Type: RemoveEvent " +
	                " EventType - " + EventType +
	                " Status - " + status);
	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Failure|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    public synchronized String ListEventAvailability(String CustID,String EventType,boolean Internal) throws Exception{
	        String message = "";
	        Boolean status = false;
	        String temp = "";
	        for (String Evnts : Events.keySet()) {
	            if (Evnts.equalsIgnoreCase(EventType)) {
	                for (String EventID : Events.get(EventType).keySet()) {
	                    message = message + "Event type: " + EventType + " ";
	                    message = message + "Event Id: " + EventID + "    Capacity: "
	                            + Events.get(EventType).get(EventID) + "\n";
	                    status = true;
	                }
	            }
	        }

	        if(!Internal) {
	            String[] NumServers = OtherServers.split(",");
	            for(int cnt = 0;cnt<NumServers.length;cnt ++) {
	                SetUDP(NumServers[cnt]);
	                temp =  sendUDPMessage("ListEventAvailability", TargetPort, EventType, "", CustID, 0,"","");

	                if (temp.substring(0, temp.indexOf("|")).trim().equals("Success")) {
	                    temp = temp.substring(temp.indexOf("|") + 1);
	                    status = true;
	                    message = message + " " + temp;
	                }
	                //else
	                  //  message = message.substring(message.indexOf("|") + 1);
	            }
	        }
	        if(!status){
	            Serverlog.WriteLog("No Event records found for type - " + EventType);
	        }
	        /*if(message.length() == 0 || message == null || message == ""){
	            Serverlog.WriteLog("No Event records found for type - " + EventType);
	        }
	        else {
	            status = true;
	        }*/

	        Serverlog.WriteLog(" Request Type: ListEventAvailability " +
	                " Request Parameters: ClientID - " + CustID +
	                " EventType - " + EventType +
	                " Status - " + status);
	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Success|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;
	    }
	    
	    protected synchronized String sendUDPMessage (String Action, int serverPort, String EventType, String EventID, String UserID, int Capacity, String NewEventType, String NewEventID) throws Exception{
	        Serverlog.WriteLog(" Server sending UDP request to " + TargetServer +
	                           " Server Port: "+ serverPort +" Action: " + Action+ " Event Type: "+ EventType+
	                           " Event Id: "+EventID+ " User Id: "+UserID+" Capacity: "+Capacity);
	        DatagramSocket aSocket = null;
	        String requestmsg = "";
	        String msg= "";
	        try {
	            requestmsg = TargetServer + "," + Action + "," + UserID + "," + EventType + "," + EventID + "," + Capacity+ "," + NewEventType+ "," + NewEventID;
	            aSocket = new DatagramSocket();
	            byte[] message = requestmsg.getBytes();
	            InetAddress aHost = InetAddress.getByName("localhost");
	            DatagramPacket request = new DatagramPacket(message, requestmsg.length(), aHost, serverPort);
	            aSocket.send(request);
	            Serverlog.WriteLog("UDP Request message sent to " + TargetServer + " port: "+ serverPort + " is: "
	                    + new String(request.getData()));
	            byte[] buffer = new byte[5000];
	            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
	            aSocket.receive(reply);
	            //response = new String(reply.getData()).substring(0,reply.getLength()).trim();
	            String response = new String(buffer,0,reply.getLength());
	            response = response.trim();
	            Serverlog.WriteLog("UDP response received from the server with port number " + serverPort + " is: "
	                    + response + " Length: " + reply.getLength());
	            msg = response;
	        } catch (SocketException e) {
	            msg = "Socket: " + e.getMessage();
	            Serverlog.WriteLog(msg);
	        } catch (IOException e) {
	            msg = "IO: " + e.getMessage();
	            Serverlog.WriteLog(msg);
	        } finally {
	            if (aSocket != null)
	                aSocket.close();
	        }
	        return msg;
	    }

	    protected void receive() throws Exception {
	        String replymsg;
	        try {
	            aSocket = new DatagramSocket(UDPPort);
	            String NewEvntType = "";
	            String NewEvntID = "";
	            System.out.println(Server + " UDP Server Started port: " + UDPPort);
	            Serverlog.WriteLog(Server + " UDP Server Started port: " + UDPPort);
	            while (true) {
	                byte[] buffer = new byte[5000];
	                DatagramPacket request = null;
	                request = new DatagramPacket(buffer, buffer.length);
	                aSocket.receive(request);
	                replymsg = new String(request.getData());
	                Serverlog.WriteLog(" Server received UDP request " + replymsg );
	                String requestmsg[];
	                String responsemsg = "";
	                requestmsg = replymsg.split(",");
	                String Srvr = requestmsg[0].trim();
	                String Action = requestmsg[1].trim();
	                String UsrID = requestmsg[2].trim();
	                String EvntType = requestmsg[3].trim();
	                String EvntID = requestmsg[4].trim();
	                int Capcity = Integer.parseInt(requestmsg[5].trim());
	                if(requestmsg.length > 5) {
	                     NewEvntType = requestmsg[6].trim();
	                     NewEvntID = requestmsg[7].trim();
	                }

	                Serverlog.WriteLog(" Action " + Action);
	                switch (Action){
	                    case "BookEvent":
	                        responsemsg = this.BookEvent(UsrID,EvntID,EvntType);
	                        break;
	                    case "GetBookingSchedule":
	                        responsemsg = this.GetBookingSchedule(UsrID, true);
	                        break;
	                    case "CancelEvent":
	                        responsemsg = this.CancelEvent(UsrID,EvntID,EvntType);
	                        break;
	                    case "AddEvent":
	                        responsemsg = this.AddEvent(UsrID,EvntID,EvntType,Capcity);
	                        break;
	                    case "RemoveEvent":
	                        responsemsg = this.RemoveEvent(EvntID,EvntType);
	                        break;
	                    case "ListEventAvailability":
	                        responsemsg = this.ListEventAvailability(UsrID,EvntType,true);
	                        break;
	                    case "GetBookingCount":
	                        responsemsg = String.valueOf(this.GetBookingCount(UsrID,EvntType));
	                        break;
	                    case "SwapEvent":
	                        responsemsg = String.valueOf(this.SwapEvent(UsrID,NewEvntID,NewEvntType,EvntID,EvntType,true));
	                        break;
	                }
	                Serverlog.WriteLog(" UDP response " + responsemsg);
	                byte[] response = responsemsg.getBytes();
	                DatagramPacket reply = new DatagramPacket(response, responsemsg.length(), request.getAddress(), request.getPort());
	                aSocket.send(reply);
	                Serverlog.WriteLog(" UDP response sent");
	            }
	        } catch (SocketException e) {
	            Serverlog.WriteLog("Socket: " + e.getMessage());
	        } catch (IOException e) {
	            Serverlog.WriteLog("IO: " + e.getMessage());
	        } finally {
	            if (aSocket != null)
	                aSocket.close();
	        }
	    }
	    protected synchronized void SetUDP(String Server){
	        switch (Server){
	            case "MTL":
	                TargetServer = "MTL";
	                TargetPort = 3333;
	                break;
	            case "TOR":
	                TargetServer = "TOR";
	                TargetPort = 4444;
	                break;
	            case "OTW":
	                TargetServer = "OTW";
	                TargetPort = 5555;
	                break;
	        }
	    }
	    protected String ProcessRequest(String replymsg) throws Exception{
	        String requestmsg[];
	        String responsemsg = "";
	        requestmsg = replymsg.split(",");
	        String Srvr = requestmsg[0].trim();
	        String Action = requestmsg[1].trim();
	        String UsrID = requestmsg[2].trim();
	        String EvntType = requestmsg[3].trim();
	        String EvntID = requestmsg[4].trim();
	        int Capcity = Integer.parseInt(requestmsg[5].trim());
	        Serverlog.WriteLog(" Action " + Action);
	        switch (Action){
	            case "BookEvent":
	                responsemsg = this.BookEvent(UsrID,EvntID,EvntType);
	                break;
	            case "GetBookingSchedule":
	                responsemsg = this.GetBookingSchedule(UsrID, true);
	                break;
	            case "CancelEvent":
	                responsemsg = this.CancelEvent(UsrID,EvntID,EvntType);
	                break;
	            case "AddEvent":
	                responsemsg = this.AddEvent(UsrID,EvntID,EvntType,Capcity);
	                break;
	            case "RemoveEvent":
	                responsemsg = this.RemoveEvent(EvntID,EvntType);
	                break;
	            case "ListEventAvailability":
	                responsemsg = this.ListEventAvailability(UsrID,EvntType,true);
	                break;
	        }
	        Serverlog.WriteLog(" UDP response " + responsemsg);
	        return responsemsg;
	    }
	    protected  int GetBookingCount(String CustID, String Month){
	        int cnt = 0;
	        if (Users.containsKey(CustID)) {
	            for (Map.Entry<String, ArrayList<String>> entry : Users.get(CustID).entrySet()) {
	                for(String evnt: entry.getValue()) {
	                    if(evnt.substring(6,8).equals(Month))
	                        cnt = cnt + 1;
	                }
	            }
	        }
	        return cnt;
	    }
	    public synchronized String SwapEvent (String CustID, String NewEventID, String NewEventType, String OldEventID, String OldEventType, boolean internal)throws Exception{
	        String message = "";
	        String Event = OldEventID.substring(0, 3);
	        String Month = OldEventID.substring(6, 8);
	        String NewEvent = NewEventID.substring(0, 3);
	        String NewMonth = NewEventID.substring(6, 8);
	        boolean status = false;
	        int CurrentCapacity = 0;
	        boolean duplicate = false;
	        boolean found = false;
	        boolean fail = false;

	        if (Event.equalsIgnoreCase(Server)) {
	            if (Users.containsKey(CustID)) {
	                for (Map.Entry<String, ArrayList<String>> eventEntry : Users.get(CustID).entrySet()) {
	                    if (eventEntry.getValue().contains(OldEventID) && eventEntry.getKey().equalsIgnoreCase(OldEventType)) {
	                        found = true;
	                        break;
	                    }
	                }
	            }
	            if(found) {
	                if (!NewEvent.equalsIgnoreCase(Server)) {
	                    int Othercnt = 0;
	                    String[] NumServers = OtherServers.split(",");
	                    for (int cnt = 0; cnt < NumServers.length; cnt++) {
	                        SetUDP(NumServers[cnt]);
	                        Othercnt = Othercnt + Integer.parseInt(sendUDPMessage("GetBookingCount", TargetPort, NewMonth, "", CustID, 0, "", ""));
	                    }
	                    Serverlog.WriteLog(" Booking count from other servers " + Othercnt);
	                    if (Othercnt > 2) {
	                        fail = true;
	                        message = " Booked more than 3 events from other servers, cannot swap";
	                        Serverlog.WriteLog(" Booked more than 3 events from other servers, cannot swap");
	                    }
	                    if (!fail) {
	                        SetUDP(NewEvent);
	                        message = sendUDPMessage("BookEvent", TargetPort, NewEventType, NewEventID, CustID, 0, "", "");

	                        if (message.substring(0, message.indexOf("|")).trim().equals("Success")) {
	                            message = "Swap is successfull, " + message.substring(message.indexOf("|") + 1);
	                            this.CancelEvent(CustID, OldEventID, OldEventType);
	                            status = true;
	                        } else
	                            message = "Swap is unsuccessfull, " + message.substring(message.indexOf("|") + 1);
	                    }
	                }
	                else{
	                    message = this.BookEvent(CustID,NewEventID,NewEventType);
	                    if (message.substring(0, message.indexOf("|")).trim().equals("Success")) {
	                        message = "Swap is successfull, " + message.substring(message.indexOf("|") + 1);
	                        this.CancelEvent(CustID, OldEventID, OldEventType);
	                        status = true;
	                    } else
	                        message = "Swap is unsuccessfull, " + message.substring(message.indexOf("|") + 1);
	                }
	            }
	            else
	            	message = "Swap is unsuccessfull, No event record found";

	        }
	        else{
	            SetUDP(Event);
	            message = sendUDPMessage("SwapEvent", TargetPort, OldEventType, OldEventID, CustID, 0,NewEventType,NewEventID);
	            if (message.substring(0, message.indexOf("|")).trim().equals("Success")) {
	                message = message.substring(message.indexOf("|") + 1);
	                //this.CancelEvent(CustID, OldEventID, OldEventType);
	                status = true;
	            } else
	                message = message.substring(message.indexOf("|") + 1);
	        }
	        Serverlog.WriteLog(" Request Type: Book Event " +
	                " Request " +
	                "Parameters: ClientID - " + CustID +
	                " OldEventID - " + OldEventID +
	                " OldEventType - " + OldEventType +
	                " NewEventID - " + NewEventID +
	                " NewEventType - " + NewEventType +
	                " Status - " + status);

	        if(status)
	            message = "Success|"+message;
	        else
	            message = "Failure|"+message;
	        Serverlog.WriteLog("Server response: " + message);
	        return message;

	    }
	}


