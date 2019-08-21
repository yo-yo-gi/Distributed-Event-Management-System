package serverandclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.omg.CORBA.ORB;

import customerBookingApp.CustomerManagerInterfacePOA;

public class FrontEnd extends CustomerManagerInterfacePOA{

	//Req identifier, [status, result, req time, response time]
	HashMap<Integer, ArrayList<String>> requestinfoReplica1;
	HashMap<Integer, ArrayList<String>> requestinfoReplica2;
	HashMap<Integer, ArrayList<String>> requestinfoReplica3;
	
	int failCountReplica1=0;
	int failCountReplica2=0;
	int failCountReplica3=0;
	
	long default_dynamicsleepvalue=5000;
	long dynamicsleepvalue=default_dynamicsleepvalue;
	
	int currentReqID=0;
	boolean isSoftwarebug=false;
	
	 int replicafailuresendPort = 9001;
	 String replicafailuresendHost1 = "132.205.46.87"; //IP of replica1
	 String replicafailuresendHost2 = "132.205.46.79";
	 String replicafailuresendHost3 = "132.205.46.80";
	 String replicafailureMsg = "FAILED";
	
	 String mulitcastHost = "localhost";

	 int multicastPort =  4555; //Port of sequencer where it will receive the data from front end, it is present in sequencer as well
	
	 int recPortRep1 = 4000;
	 int recPortRep2 = 4001;
	 int recPortRep3 = 4002;
	 
	 int checkResponseReceivedFlag=0;
	 
	 synchronized void  updateResponseReceivedFlag() {
		 checkResponseReceivedFlag++;
		 System.out.println("checkResponseReceivedFlag" + checkResponseReceivedFlag);
	 }
	
	 
	
	 String finalRequestData = "";
	
    private String userID;
    private Logger logger;
	private ORB orb;
		
		public void setORB(ORB orb_val) {
			orb = orb_val;
		}

		public void shutdown() {
			orb.shutdown(false);
		}
	
		FrontEnd() {

	        // RECEVIE THREAD ->  // 1) Upon response, update the hashmap 
	        // 2) in the result get the first entry of delimiiter "|" -> Success or Failure
	        //Initialize the logger
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
	        System.setProperty("input_file_name", ".//frontEndLogs//"+userID+ "_"+ dateFormat.format(new Date()));
	        logger = LogManager.getLogger(FrontEnd.class);
	        PropertyConfigurator.configure("G:\\Final_Project\\Final_Project\\log4j_ds.properties");

	        requestinfoReplica1= new HashMap<Integer, ArrayList<String>>(); 
	    	requestinfoReplica2= new HashMap<Integer, ArrayList<String>>();
	    	requestinfoReplica3= new HashMap<Integer, ArrayList<String>>();
	        
	    	Runnable task1 = () -> {
	    		receiveUDPRequest1(recPortRep1);
	        };
	        Thread thread1 = new Thread(task1);
	        thread1.start();
	    	
	    	//UDPReceive udpresponse2 = new UDPReceive(this,recPortRep2);
	    	Runnable task2 = () -> {
	    		receiveUDPRequest2(recPortRep2);
	        };
	        Thread thread2 = new Thread(task2);
	        thread2.start();
	    	//UDPReceive udpresponse3 = new UDPReceive(this,recPortRep3);
	    	Runnable task3 = () -> {
	    		receiveUDPRequest3(recPortRep3);
	        };
	        Thread thread3 = new Thread(task3);
	        thread3.start();

	    }

	    
	    public  String addEvent(String userID, String eventID, String eventType, String capacity) {
	    	System.out.println("Reached addevent");
	    	String params = userID + "|" +  eventID + "|" + eventType + "|" +capacity;
	    	System.out.println("Params in add event is "+params);
	    	String response =  processRequest("addEvent" , params);
	    	return response;
	    }

	    public  String removeEvent(String userID, String eID, String eType) {
	    	System.out.println("Reached removeEvent");
	    	String params = userID + "|" +  eID + "|" +eType;
	    	String response =  processRequest("removeEvent" , params);
	    	return response;	   
	    	}

	    public  String listEventAvailability(String userID, String eventType) {
	    	System.out.println("Reached listEventAvailability");
	    	String params = userID + "|" +  eventType;
	    	String response =  processRequest("listEventAvailability" , params);
	    	return response;
	    }

	    public  String bookEvent(String userID, String custID, String eID, String eType) {
	    	System.out.println("Reached bookEvent");
	    	String params = userID + "|" +  custID + "|" + eID + "|" + eType;
	    	String response =  processRequest("bookEvent" , params);
	    	return response;
	    }

	    public  String getBookingSchedule(String userID, String customerID) {
	    	System.out.println("Reached getBookingSchedule");
	    	String params = userID + "|" +  customerID;
	    	String response =  processRequest("getBookingSchedule" , params);
	    	return response;
	    }

	    public  String cancelEvent(String userID, String custID, String eID, String eType) {
	    	System.out.println("Reached cancelEvent");
	    	String params = userID + "|" +  custID + "|" +eID + "|" + eType;
	    	String response =  processRequest("cancelEvent" , params);
	    	return response;
	    }

	    public  String swapEvent(String userID, String custID, String newEventID, String newEventType, String oldEventID, String oldEventType ){
	    	System.out.println("Reached swapEvent");
	    	String params = userID + "|" +  custID + "|" + newEventID +"|" + newEventType + "|" +oldEventID + "|" + oldEventType;
	    	String response =  processRequest("swapEvent" , params);
	    	return response;
	    }

	    public String processRequest(String methodName, String inputParams) {
	    	//Get the input in string format
	    	//split the input into Method name and input params
	    	//create 3 hashmap, for each request
	    	System.out.println("Input params"+ inputParams);
	    	String[] words = inputParams.split("\\|");
	    	
	    	String custID;
	    	String newEventID;
	    	String newEventType;
	    	String oldEventID;
	    	String oldEventType;
	    	String eventID;
	    	String eventType;
	    	String capacity;
	    	String userID;
	    	
	    	 finalRequestData=currentReqID+"|";
	    	
	    	switch(methodName) {

	    	case "addEvent":
	    		
	    		userID = words[0];
	    		eventID = words[1];
	    		eventType =  words[2];
	    		capacity = words[3];
	    		System.out.println(words[0]+"  "+words[1]+"  "+words[2]);
	    		finalRequestData = finalRequestData + userID + "|" + "M_ADD_EVENT" + "|"+eventType + "|" + eventID + "|" + capacity + "|" + "NULL" + "|" + "NULL"+ "|" + userID;
	    		System.out.println("finalRequestData " + finalRequestData);
	    		break;
	    		
	    	case "removeEvent":
	    		userID = words[0];
	    		eventID = words[1];
	    		eventType = words[2];
	    		
	    		finalRequestData = finalRequestData + userID + "|" + "M_REMOVE_EVENT"+ "|" + eventType + "|" + eventID + "|" + "NULL" + "|" + "NULL" + "|" + "NULL"+ "|" + userID;
	    		break;
	    		
	    	case "listEventAvailability":
	    		userID = words[0];
	    		eventType = words[1];
	    		finalRequestData = finalRequestData + userID + "|" + "M_LIST_EVENT"+ "|" + eventType + "|" + "NULL" + "|" + "NULL" + "|" + "NULL" + "|" + "NULL"+ "|" + userID;
	    		break;
	    		
	    	case "bookEvent":
	    		userID = words[0];
	    		custID = words[1];
	    		eventID = words[2];
	    		eventType =  words[3];
	    		finalRequestData = finalRequestData + custID + "|" + "C_BOOK_EVENT" + "|"+ eventType + "|" + eventID + "|" + "NULL" + "|" + "NULL" + "|" + "NULL" + "|" + userID;
	    		
	    		break;
	    		
	    	case "getBookingSchedule":
	    		userID = words[0];
	    		custID = words[1];
	    		
	    		finalRequestData = finalRequestData + custID + "|" + "C_GET_SCHEDULE" + "|"+ "NULL" + "|" + "NULL" + "|" + "NULL" + "|" + "NULL" + "|" + "NULL"+ "|" + userID;
	    		
	    		break;
	    		
	    	case "cancelEvent":
	    		userID = words[0];
	    		custID = words[1];
	    		eventID = words[2];
	    		eventType =  words[3];
	    		finalRequestData = finalRequestData + custID + "|" + "C_CANCEL_EVENT" + "|"+ eventType + "|" + eventID + "|" + "NULL" + "|" + "NULL" + "|" + "NULL"+ "|" + userID;
	    		
	    		break;
	    	
	    	case "swapEvent":
	    		userID = words[0];
	    		custID = words[1];
	    		newEventID = words[2];
	    		newEventType = words[3];
	    		oldEventID = words[4];
	    		oldEventType = words[5];
	    		
	    		finalRequestData = finalRequestData + custID + "|" + "C_SWAP_EVENT" + "|"+ newEventType + "|" + newEventID + "|" + "NULL" + "|" + oldEventType + "|" + oldEventID+ "|" + userID;
	    		
	    		break;
	    		
	    	default:
	    		System.out.println("ERROR!! Can not identify the method name");
	    	
	    	}

	    	currentReqID++; //Increment the req id
	    	//checkResponseReceivedFlag = 0;
	    	//Start 3 UDP receivers on front end for different replicas 
	    	//UDPReceive udpresponse1 = new UDPReceive(this,recPortRep1);
	    	
	    	
	    	checkResponseReceivedFlag = 0;
	    	Runnable task = () -> {
	    		sendUDPRequest(mulitcastHost,multicastPort, finalRequestData);
	    	};
	    	
	    	Thread thread = new Thread(task);
	        thread.start();
	    	
	    	if(isSoftwarebug == true) 
	    	
	    	{
	    		//sleep(2);
	    		//Wait till all the responses are received
	    		System.out.println("Before the flag check while in softwarebug"  );
	    		while(checkResponseReceivedFlag != 3) {
	    			try {
					thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		}
	    		
	    		
	    		
	    		System.out.println("Changed the value of flag in Softwarebug to "+checkResponseReceivedFlag);
	    		
	    		// query the request hashmap and validate results
	    		System.out.println("Value of currentReqID is "+currentReqID);
	    		ArrayList<String> responseAttri1= requestinfoReplica1.get(currentReqID);
	    		ArrayList<String> responseAttri2= requestinfoReplica2.get(currentReqID);
	    		ArrayList<String> responseAttri3= requestinfoReplica3.get(currentReqID);
	    		
	    		//Now compare the results
	    	    // in the result get the first entry of delimiter "|" -> Success or Failure
	    		HashMap<String, Integer> s_f_count = new HashMap<String, Integer>();
	    		s_f_count.put("success", 0);
	    		s_f_count.put("failure", 0);
	    		
	    		System.out.println("responseAttri1 --->>> " + responseAttri1);
	    		System.out.println("Getting the map for  this id ++++++++ "+currentReqID);
	    		
	    		System.out.println("RM1 HASHMAP : " + requestinfoReplica1.get(currentReqID).get(1));
	    		System.out.println("RM2 HASHMAP : " + requestinfoReplica2.get(currentReqID).get(1));
	    		System.out.println("RM3 HASHMAP : " + requestinfoReplica3.get(currentReqID).get(1));
	    		//if(responseAttri1.get(0).equals("1")) {//If request was successful then, increment the corresponding response entry
	    		if(responseAttri1.get(0).equals("1")) {	
	    			String resp = requestinfoReplica1.get(currentReqID).get(1);
	    			System.out.println("Response value for req "+currentReqID + " from Replica1 at frontend is "+resp);
	    			
	    			
	    			if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("success") || 
	    					resp.substring(0,resp.indexOf('|')).toLowerCase().equals("failure")
	    					) {
	    				s_f_count.put(resp.substring(0,resp.indexOf('|')).toLowerCase(), s_f_count.get(resp.substring(0,resp.indexOf('|')).toLowerCase())+1); //Increased the count
	    			}
	    			else {
	    				s_f_count.put("failure", s_f_count.get("failure")+1);
	    			}
	    			
	    		}
	    		else {
	    			//Replica1 failed
	    			failCountReplica1++;
	    			if(failCountReplica1 > 3) {
	    				//
	    				System.out.println("Calling the method of replica1 to recover");
	    				
						try {
							InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost1);
							UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
		    				failCountReplica1=0;
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    				
	    			}
	    			
	    			
	    		}
	    		if(responseAttri2.get(0).equals("1")) {//If request was successful then, increment the corresponding response entry 
	    			String resp = requestinfoReplica2.get(currentReqID).get(1);
	    			System.out.println("Response value for req "+currentReqID + " from Replica2 at frontend is "+resp);
	    			if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("success") || 
	    					resp.substring(0,resp.indexOf('|')).toLowerCase().equals("failure")
	    					) {
	    				s_f_count.put(resp.substring(0,resp.indexOf('|')).toLowerCase(), s_f_count.get(resp.substring(0,resp.indexOf('|')).toLowerCase())+1); //Increased the count
	    			}
	    			else {
	    				s_f_count.put("failure", s_f_count.get("failure")+1);
	    			}
	    			
	    		}
	    		else {
	    			//Replica2 failed
	    			failCountReplica2++;
	    			if(failCountReplica2 > 3) {
	    				//
	    				System.out.println("Calling the method of replica2 to recover");
	    				
	    				try {
							InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost2);
							UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
		    				failCountReplica2=0;
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    		}
	    		if(responseAttri3.get(0).equals("1")) {//If request was successful then, increment the corresponding response entry 
	    			String resp = requestinfoReplica3.get(currentReqID).get(1);
	    			System.out.println("Response value for req "+currentReqID + " from Replica3 at frontend is "+resp);
	    			if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("success") || 
	    					resp.substring(0,resp.indexOf('|')).toLowerCase().equals("failure")
	    					) {
	    				s_f_count.put(resp.substring(0,resp.indexOf('|')).toLowerCase(), s_f_count.get(resp.substring(0,resp.indexOf('|')).toLowerCase())+1); //Increased the count
	    			}
	    			else {
	    				s_f_count.put("failure", s_f_count.get("failure")+1);
	    			}
	    			
	    		}
	    		else {
	    			//Replica3 failed
	    			failCountReplica3++;
	    			if(failCountReplica3 > 3) {
	    				//
	    				System.out.println("Calling the method of replica3 to recover");
	    				
	    				try {
							InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost3);
							UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
		    				failCountReplica3=0;
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    		}
	    		
	    		
	    		// if cnter is >3 then send request to RM1/2/3 to recover from other servers;
	    		//cnt reset
	    		
	    		
	    		
	    			if(s_f_count.get("success") ==3) {
	    			//success response from all 
	    				return requestinfoReplica1.get(currentReqID).get(1); //Sending response from replica 1
	    			}
	    			if(s_f_count.get("failure") ==3) {
		    			//fail response from all
		    				return requestinfoReplica1.get(currentReqID).get(1); //Sending response from replica 1
		    		}
	    			
	    			ArrayList<HashMap<Integer, ArrayList<String>>> a = new ArrayList<HashMap<Integer, ArrayList<String>>>();
	    			a.add(requestinfoReplica1);
	    			a.add(requestinfoReplica2);
	    			a.add(requestinfoReplica3);
	    			
	    			String finalResp="Blank";
	    			if(s_f_count.get("success") > s_f_count.get("failure")) {
	    				for(int i=0; i<3; i++) {
	    					String resp=a.get(i).get(currentReqID).get(1);
	    					if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("success")) {
	    						finalResp = resp;	
	    					}
	    					if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("failure")) {
	    						HashMap<Integer, ArrayList<String>> Replica = a.get(i);
	    						if(Replica == (requestinfoReplica1)) {failCountReplica1++;}
	    						if(Replica == (requestinfoReplica2)) {failCountReplica2++;}
	    						if(Replica == (requestinfoReplica3)) {failCountReplica3++;}
	    						System.out.println("failCountReplica1 " + failCountReplica1 );
	    						if(failCountReplica1 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica1 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost1);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica1=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						System.out.println("failCountReplica2 " + failCountReplica2 );
	    						if(failCountReplica2 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica2 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost2);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica2=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						System.out.println("failCountReplica3 " + failCountReplica3 );
	    						if(failCountReplica3 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica3 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost3);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica3=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						//System.out.println("Replica " + Replica + "requestinfoReplica1" +requestinfoReplica1 + "requestinfoReplica2" + requestinfoReplica2 + "requestinfoReplica3" + requestinfoReplica3);
	    						
	    						
	    					}
	    				}
	    				
	    				return finalResp;	
	    			}
	    			
	    			
	    			if(s_f_count.get("success") < s_f_count.get("failure")) {
	    				for(int i=0; i<3; i++) {
	    					String resp=a.get(i).get(currentReqID).get(1);
	    					if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("failure")) {
	    						finalResp = resp;	
	    					}
	    					if(resp.substring(0,resp.indexOf('|')).toLowerCase().equals("success")) {
	    						HashMap<Integer, ArrayList<String>> Replica = a.get(i);
	    						if(Replica == (requestinfoReplica1)) {failCountReplica1++;}
	    						if(Replica == (requestinfoReplica2)) {failCountReplica2++;}
	    						if(Replica == (requestinfoReplica3)) {failCountReplica3++;}
	    						if(failCountReplica1 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica1 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost1);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica1=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						
	    						if(failCountReplica2 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica2 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost2);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica2=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						
	    						if(failCountReplica3 > 3) {
	    		    				//
	    		    				System.out.println("Calling the method of replica3 to recover");
	    		    				
	    		    				try {
	    								InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost3);
	    								UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    			    				failCountReplica3=0;
	    							} catch (UnknownHostException e) {
	    								// TODO Auto-generated catch block
	    								e.printStackTrace();
	    							}
	    		    			}
	    						
	    						
	    						
	    					}
	    				}
	    				return finalResp;	
	    			}
	    			
	    		
	    	}
	    	
	    	else {
	    		
	    		System.out.println("Entered the high availability case");
	    		try {
					Thread.sleep(dynamicsleepvalue);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		// query the request hashmap and validate results
	    		
	    		ArrayList<String> responseAttri1= requestinfoReplica1.get(currentReqID);
	    		ArrayList<String> responseAttri2= requestinfoReplica2.get(currentReqID);
	    		ArrayList<String> responseAttri3= requestinfoReplica3.get(currentReqID);
	    		
	    		//Now compare the results
	    	    // in the result get the first entry of delimiiter "|" -> Success or Failure
	    		//HashMap<String, Integer> s_f_count = new HashMap<String, Integer>();
	    		//s_f_count.put("success", 0);
	    		//s_f_count.put("failure", 0);
	    		
	    		boolean isrecov1=false;
	    		boolean isrecov2=false;
	    		boolean isrecov3=false;
	    		
	    		if(requestinfoReplica1.get(currentReqID).get(0).equals("0")) {//Request is failed
	    			
	    			isrecov1=true;
	    			System.out.println("High : calling replica1 to recover");
	    			
	    			try {
						InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost1);
						UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			
	    		}
	    		
	    		if(requestinfoReplica2.get(currentReqID).get(0).equals("0")) {//Request is failed
	    			
	    			isrecov2=true;
	    			System.out.println("High : calling replica2 to recover");
	    			try {
						InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost2);
						UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    				
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}

	    		if(requestinfoReplica3.get(currentReqID).get(0).equals("0")) {//Request is failed
	
	    			isrecov3=true;
	    			System.out.println("High : calling replica3 to recover");
	    			try {
						InetAddress replicaHost = InetAddress.getByName(replicafailuresendHost3);
						UDPSend u = new UDPSend(this, replicaHost, replicafailuresendPort, currentReqID,replicafailureMsg );
	    				
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}	    		
	    		
	    		// if status is false and result is blank - that RM is crashed , ask it recover
	    		
	    		
	    		// get the slowest result time by diff (resp time - req time in the hashmap ) and update the seep time variable - slowest * 2;
			if (null != responseAttri1.get(3) && null != responseAttri2.get(3) && null != responseAttri3.get(3)) {
				long timediff1 = Long.parseLong(responseAttri1.get(3)) - Long.parseLong(responseAttri1.get(2));
				long timediff2 = Long.parseLong(responseAttri2.get(3)) - Long.parseLong(responseAttri2.get(2));
				long timediff3 = Long.parseLong(responseAttri3.get(3)) - Long.parseLong(responseAttri3.get(2));

				if (timediff1 >= timediff2 && timediff1 >= timediff3) {
					System.out.println("Time taken by replica1 to respond: "+timediff1);
					dynamicsleepvalue = timediff1 * 2 / (1000000);
				} // Check the nano to second conversion
				else if (timediff2 >= timediff1 && timediff2 >= timediff3) {
					System.out.println("Time taken by replica2 to respond: "+timediff2);
					dynamicsleepvalue = timediff2 * 2 / (1000000);
				} else if (timediff3 >= timediff1 && timediff3 >= timediff2) {
					System.out.println("Time taken by replica3 to respond: "+timediff3);
					dynamicsleepvalue = timediff3 * 2 / (1000000);
				}

			}
			else {
				dynamicsleepvalue = default_dynamicsleepvalue;//Default 
			}
	    		
			System.out.println("New dynamic sleep value is: "+dynamicsleepvalue);
	    		//dynamicsleepvalue = dynamicsleepvalue*100;
	    		//int temp = (int)dynamicsleepvalue;
	    		//if(temp<1) {dynamicsleepvalue=1;}
	    		//else dynamicsleepvalue = temp;
				dynamicsleepvalue += 1000; //Adding 1 second to final dynamicvalue
	    		if(!isrecov1){return requestinfoReplica1.get(currentReqID).get(1);}
	    		else if(!isrecov2){return requestinfoReplica2.get(currentReqID).get(1);}
	    		else if(!isrecov3){return requestinfoReplica3.get(currentReqID).get(1);}
	    		
	    	}
	    	return "DUMMY";
	    }

	    
	    public void sendUDPRequest(String host, int requestPort, String requestVal){
	        //This method will call the send to send the data
	            DatagramSocket aSocket=null;
	            try {
	            	
	                InetAddress requestHost = InetAddress.getByName("localhost");
	                //System.out.println("val of requestval "+requestVal);
	                //System.out.println("val of requestHost "+requestHost);
	                //System.out.println("val of requestPort "+requestPort);

	                DatagramPacket messageOut = new DatagramPacket(requestVal.getBytes(), requestVal.length(),requestHost,requestPort);
	                
	                if(!requestVal.equals(replicafailureMsg)) {
	                ArrayList<String> attr1 = new ArrayList<String>();
	                
	                attr1.add(0, "0"); //Adding initial state as failed
	                long starttime = System.nanoTime();
	                attr1.add(1,null);
	                attr1.add(2,String.valueOf(starttime));//Added start time of the request
	                attr1.add(3,null);
	                ArrayList<String> attr2 = new ArrayList<String>();
	                
	                attr2.add(0, "0"); //Adding initial state as failed
	              //  long starttime = System.nanoTime();
	                attr2.add(1,null);
	                attr2.add(2,String.valueOf(starttime));//Added start time of the request
	                attr2.add(3,null);
	                ArrayList<String> attr3 = new ArrayList<String>();
	                
	                attr3.add(0, "0"); //Adding initial state as failed
	               // long starttime = System.nanoTime();
	                attr3.add(1,null);
	                attr3.add(2,String.valueOf(starttime));//Added start time of the request
	                attr3.add(3,null);
	                aSocket = new DatagramSocket();
	                
	                requestinfoReplica1.put(currentReqID, attr1);
	                requestinfoReplica2.put(currentReqID, attr2);
	                requestinfoReplica3.put(currentReqID, attr3);
	                }
	                System.out.println("Replica1 in UDP send---->  "+ requestinfoReplica1.get(currentReqID));
	                
	                System.out.println("Sending this message from UDP send "+ requestVal + " on "+ requestPort);
	                aSocket.send(messageOut);

	            } catch (SocketException e) {
	                System.out.println("Socket: " + e.getMessage());
	            } catch (IOException e) {
	                System.out.println("IO: " + e.getMessage());
	            } finally {
	                if (aSocket != null)
	                    aSocket.close();
	            }
	        }

	    
	private void receiveUDPRequest1(int receivePort) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(receivePort);
			byte[] buffer = new byte[2048];
			System.out.println("UDP Server " + receivePort + " Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);

				long endtime = System.nanoTime();

				String response1 = new String(request.getData());
				response1 = response1.trim();
				System.out.println("Received data from replica manager through +" + receivePort + " port " + response1);

				// After receiving the data, fill the corresponding replica information

				// For Replica 1,
				// using the same request as as of front end as we will not have any other
				// requests
				synchronized (this) {
					ArrayList<String> attr = requestinfoReplica1.get(currentReqID);
					attr.set(0, "1");// Here string 1 is for successful request
					attr.set(1, response1);
					attr.set(3, String.valueOf(endtime));
					requestinfoReplica1.put(currentReqID, attr);
					System.out.println("in UDP Receive 1 --->>> " + requestinfoReplica1.get(currentReqID).get(1));
					checkResponseReceivedFlag++;
				}

				// System.out.println("Changed the map for this id ++++++++
				// "+this.currentReqID);
				// System.out.println("Arraylist1 "+ attr);

				// System.out.println("Changed the value of flag in receive to 1
				// "+checkResponseReceivedFlag);

				buffer = new byte[2048];

				// System.out.println("Changed the value of flag in receive to 2
				// "+checkResponseReceivedFlag);
				// System.out.println("Changed the map for this id ++++++++
				// "+this.currentReqID);

				//updateResponseReceivedFlag();
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
        private void receiveUDPRequest2(int receivePort) {
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket(receivePort);
                byte[] buffer = new byte[2048];
                System.out.println("UDP Server "+receivePort+" Started............");
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    long endtime = System.nanoTime();

                    String response2 = new String(request.getData());
                    response2 = response2.trim();
                    System.out.println("Received data from replica manager through +"+receivePort + " port "+response2);
                    

           
                    // if(receivePort == recPortRep2) { //this is for test comment this uncomment above line
                    	//For Replica 2
                    synchronized (this) {
                    	ArrayList<String> attr = requestinfoReplica2.get(currentReqID);
                    	attr.set(0,"1");//Here string 1 is for successful request
                    	attr.set(1,response2);
                    	attr.set(3,String.valueOf(endtime));
                    	
                    	requestinfoReplica2.put(currentReqID, attr);
                    	System.out.println("in UDP Receive 2 --->>> " +requestinfoReplica2.get(currentReqID).get(1) );
                    	checkResponseReceivedFlag++;
                    }
                    	///System.out.println("Changed the map for  this id ++++++++ "+this.currentReqID);
                    
                    
                   
                    
                   // System.out.println("Changed the value of flag in receive to 1 "+checkResponseReceivedFlag);
                    
                    
                   
                    
                    	buffer = new byte[2048];
                    
                    //System.out.println("Changed the value of flag in receive to 2 "+checkResponseReceivedFlag);
                    //System.out.println("Changed the map for  this id ++++++++ "+this.currentReqID);
                    
                    	//updateResponseReceivedFlag();
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
        private void receiveUDPRequest3(int receivePort) {
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket(receivePort);
                byte[] buffer = new byte[2048];
                System.out.println("UDP Server "+receivePort+" Started............");
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    long endtime = System.nanoTime();

                    String response3 = new String(request.getData());
                    response3 = response3.trim();
                    System.out.println("Received data from replica manager through +"+receivePort + " port "+response3);
                    
                    
                    //After receiving the data, fill the corresponding replica information
                    
                    //if(receivePort == recPortRep3) {//this is for test comment this uncomment above line
                    	//For Replica 3
                    synchronized (this) {
                    	ArrayList<String> attr = requestinfoReplica3.get(currentReqID);
                    	attr.set(0,"1");//Here string 1 is for successful request
                    	attr.set(1,response3);
                    	attr.set(3,String.valueOf(endtime));
                    	requestinfoReplica3.put(currentReqID, attr);
                    	System.out.println("in UDP Receive 3 --->>> " + requestinfoReplica3.get(currentReqID).get(1) );
                    	checkResponseReceivedFlag++;
                    }
                    	//System.out.println("Changed the map for  this id ++++++++ "+this.currentReqID);
         
                    
                   // System.out.println("Changed the value of flag in receive to 1 "+checkResponseReceivedFlag);
                    
                    
                   
                    
                    	buffer = new byte[2048];
                    
                    //System.out.println("Changed the value of flag in receive to 2 "+checkResponseReceivedFlag);
                    //System.out.println("Changed the map for  this id ++++++++ "+this.currentReqID);
                    
                    	//updateResponseReceivedFlag();
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