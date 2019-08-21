/**
 * @author Yogesh Nimbhorkar
 */
package replica1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;



public class ReplicaManager {
	
	private static MontrealServerImplementation monStub;
	private static OttawaServerImplementation ottStub;
	private static TorontoServerImplementation torStub;
	private static  Queue<String> requestQueue; 	
    private static final int FEPORT=4001, CRASH_SERVER_PORT=9001, MULTICAST_RECEIVE_PORT=8001, TEST_HA_PORT=9002; 
private static final String FE_IP_ADDRESS="132.205.46.78", SEQUENCER_IP_ADDRESS="230.1.1.5";
private static final boolean HA = true;
	
/* things to change */	public static void main(String[] args) {
		try {
			
			//starting 3 local servers
			startServers();		

			
			//instantiate requestQueue
			requestQueue=new LinkedList<String>();
			
			//starting RM1 server
			System.out.println("Starting RM1...");
			new Thread(() -> receiveMulticastRequest()).start();
			new Thread(() -> receiveCrashRequest()).start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     *	Method to recieve request by other servers
     * @throws Exception 
     */
	static void receiveMulticastRequest(){
		MulticastSocket aSocket = null;
		try {
			//to do
			aSocket = new MulticastSocket(MULTICAST_RECEIVE_PORT);
			System.out.println("RM1 Server "+MULTICAST_RECEIVE_PORT+" Started............");
			aSocket.joinGroup(InetAddress.getByName(SEQUENCER_IP_ADDRESS));

			System.out.println("RM1 Server ready and waiting ...");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				String inputData = new String(request.getData());
				DatagramPacket reply = null;
				System.out.println(inputData);
				String message = "";
				//put the request string in queue
				requestQueue.add(inputData);
				
				
				//execute request and get the result
				message=executeRequest(inputData);
				
				//send response back to Front End
				sendUDPMessage(FEPORT,  message);
				
				
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
	
	/**
	 * Method to recieve request by other servers
	 */
	static void receiveCrashRequest() {
		DatagramSocket aSocket = null;
		try {
			//to do
			aSocket = new DatagramSocket(CRASH_SERVER_PORT);
			System.out.println("CRASH RECOVERY SERVER  "+CRASH_SERVER_PORT+" Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				System.out.println("In crash recovery");
				//restarting all server after crash
				restartAfterCrash();
				
			}
		} catch (Exception e ) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}


	/**
	 * Method to send back reply to front end - UDP reply
	 * @param feport port of Front End
	 * @param message reply
	 */
	private static void sendUDPMessage(int feport, String message) {
//		message="failure|";
		message+=" from RM with port 4001";
		System.out.println("Response :"+message+"\n");
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] msg = message.getBytes(); 
			InetAddress aHost = InetAddress.getByName(FE_IP_ADDRESS);  // Address of front end
			DatagramPacket request = new DatagramPacket(msg, msg.length, aHost, feport);
			aSocket.send(request);

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	
		
	}

	/**
	 * Method to choose perticular server implementation and excecute perticular method as mentined in inputData
	 * @param inputData
	 * @return
	 * @throws Exception 
	 */
	private static String executeRequest(String inputData){

		System.out.println("Executing request: "+inputData );
		String result = "";
		
		//splitting input request
		//to do finalize format of this inputData
		String input[] = inputData.toUpperCase().split("\\|");
		String requestId=input[0];
		String customerId = input[1];
		String operation = input[2];
		String eventType = input[3];
		String eventId = input[4];		
		String capacity = input[5];
		String oldEventType = input[6];
		String oldEventId = input[7];
		String executorId = input[8];
		
//		if (HA && requestId.equalsIgnoreCase("20")) {
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		
		
		//some refinement
		customerId = customerId.trim();
		operation=operation.trim();
		
		//selecting server implementation based on customerId
		ServerInterface targetStub = null;
		String initialID = customerId.substring(0, 3).toUpperCase();
		int bookingCapacity=0;
		if (!capacity.equalsIgnoreCase("NULL") ){
			bookingCapacity=Integer.parseInt(capacity);
		}
		
		
		switch (initialID) {
		case "MTL":
			targetStub = monStub;
			break;
		case "TOR":
			targetStub = torStub;
			break;
		case "OTW":
			targetStub = ottStub;
			break;
		default:
			//throw new Exception("No Target Implemenation matching");
		}
		
		
		//selecting appropriate method based on operation type
		String targetMethod = operation.toUpperCase();
		switch (targetMethod) {
		case "C_BOOK_EVENT":
			result += targetStub.bookEvent(customerId, eventType, eventId);
			break;
		case "C_GET_SCHEDULE":
			result += targetStub.getBookingSchedule(customerId);
			break;
		case "C_CANCEL_EVENT":
			result += targetStub.cancelEvent(customerId, eventType, eventId);
			break;
		case "C_SWAP_EVENT":		
			result += targetStub.swapEvent(customerId, eventId, eventType, oldEventId, oldEventType);
			break;
		case "M_ADD_EVENT":
			result += targetStub.addEvent(eventId, eventType, bookingCapacity);
			break;
		case "M_REMOVE_EVENT":
			result += targetStub.removeEvent(eventType, eventId);
			break;
		case "M_LIST_EVENT":
			result += targetStub.listEventAvailability(eventType);
			break;
		default:
//			throw new Exception("No operation matching");
		}
		return result;
	}

	/**
	 * Method to start 3 servers
	 * @throws Exception 
	 * 
	 */
	private static void startServers() throws Exception{
		MontrealServer.startMontrealServer();
		OttawaServer.startOttawaServer();
		TorontoServer.startTorontoServer();

		//instantiating needed structures
		//creating 3 sever implementation objects for UDP call from receive method
		monStub=MontrealServer.monStub;
		ottStub=OttawaServer.otwStub;
		torStub = TorontoServer.torStub;
		
	}
	
	/**
	 * Method to restart 3 servers
	 * @throws Exception 
	 * 
	 */
	private static void restartAfterCrash() throws Exception {
		stopServers();
		startServers();
		//waiting to start all servers before executing
		Thread.sleep(2000);
		for (String currentRequest : requestQueue) {
			String response=executeRequest(currentRequest);
			System.out.println("After Crash: "+response);
			System.out.println(monStub.eventInfo);
			System.out.println(torStub.eventInfo);
			System.out.println(ottStub.eventInfo);
		}
	}
	
	
	/**
	 * Method to stop 3 servers
	 * @throws Exception 
	 * 
	 */
	public static void stopServers() throws Exception{
		
		MontrealServer.stopServer();
		OttawaServer.stopServer();
		TorontoServer.stopServer();
		
	}
	
	
/*	
	*//**
	 * Method to recieve request by other servers
	 *//*
	static void testHighAvailability() {
		DatagramSocket aSocket = null;
		try {
			//to do
			aSocket = new DatagramSocket(TEST_HA_PORT);
			System.out.println("TEST high availability  "+TEST_HA_PORT+" Started............");
			while (true) {
				byte[] bufferData = new byte[1024];
				DatagramPacket request = null;
				request = new DatagramPacket(bufferData, bufferData.length);
				aSocket.receive(request);
				System.out.println("Stopping all the sockets");
				//restarting all server after crash
				Thread.sleep(5000);
				
			}
		} catch (Exception e ) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

	}*/
}
