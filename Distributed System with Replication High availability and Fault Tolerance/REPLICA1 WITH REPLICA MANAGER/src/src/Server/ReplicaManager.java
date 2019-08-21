package src.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;



public class ReplicaManager {
	
	private static MTL monStub;
	private static OTW ottStub;
	private static TOR torStub;
	private static  Queue<String> requestQueue; 	
/* things to change */	private static final int FEPORT=4000, CRASH_SERVER_PORT=9001, MULTICAST_RECEIVE_PORT=8001; 
private static final String FE_IP_ADDRESS="132.205.46.78", SEQUENCER_IP_ADDRESS="230.1.1.5";
	
/* things to change */	public static void main(String[] args) throws Exception {
		try {
			
			//starting 3 local servers
			startServers();		

			//instantiating needed structures
			//creating 3 sever implementation objects for UDP call from receive method
			
			
			//instantiate requestQueue
			requestQueue=new LinkedList<String>();
			
			//starting RM1 server
			System.out.println("Starting RM1...");
			new Thread(() -> {
				try {
					receiveMulticastRequest();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
			new Thread(() -> receiveCrashRequest()).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     *	Method to recieve request by other servers
     * @throws Exception 
     */
	static void receiveMulticastRequest() throws Exception{
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
		System.out.println("Response: "+ message);
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
	private static String executeRequest(String inputData) throws Exception{

		String result = "";
		
		//splitting input request
		//to do finalize format of this inputData
		String input[] = inputData.split("\\|");
		String requestId=input[0];
		String customerId = input[1];
		String operation = input[2];
		String eventType = input[3];
		String eventId = input[4];		
		String capacity = input[5];
		String oldEventType = input[6];
		String oldEventId = input[7];
		String executorId = input[8];
		
//		Thread.sleep(5000);
		
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
			result += targetStub.BookEvent(customerId, eventId,eventType);
			break;
		case "C_GET_SCHEDULE":
			result += targetStub.GetBookingSchedule(customerId, false);
			break;
		case "C_CANCEL_EVENT":
			result += targetStub.CancelEvent(customerId, eventId, eventType);
			break;
		case "C_SWAP_EVENT":
			if(!oldEventId.substring(0,3).equals(customerId.substring(0,3))) {
				switch (oldEventId.substring(0,3)) {
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
		      }
			result += targetStub.SwapEvent(customerId, eventId, eventType, oldEventId, oldEventType,false);
		/*	SetProfile(this.UserID);
		    connectserver();*/
			break;
		case "M_ADD_EVENT":
			result += targetStub.AddEvent(customerId,eventId, eventType, bookingCapacity);
			break;
		case "M_REMOVE_EVENT":
			result += targetStub.RemoveEvent(eventId,eventType);
			break;
		case "M_LIST_EVENT":
			result += targetStub.ListEventAvailability(customerId,eventType,false);
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
		monStub=new MTL();
		ottStub=new OTW();
		torStub = new TOR();
	}
	
	/**
	 * Method to restart 3 servers
	 * @throws Exception 
	 * 
	 */
	private static void restartAfterCrash() throws Exception {
		stopServers();
		startServers();
		for (String currentRequest : requestQueue) {
			String response=executeRequest(currentRequest);
			System.out.println("In crash restart :"+response);
		}
	}
	
	
	/**
	 * Method to stop 3 servers
	 * @throws Exception 
	 * 
	 */
	private static void stopServers() throws Exception{
		
		MTL.stopServer();
		OTW.stopServer();
		TOR.stopServer();
		
	}
}
