package serverandclient;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

//This class is used only by front end

	public class UDPSend{

		FrontEnd frontend;
	    InetAddress requestHost;
	    int requestPort;
	    int requestNo;
	    String requestVal;
	    

	    UDPSend(FrontEnd frontend, InetAddress requestHost, int port, int requestNo, String requestVal){
	        this.frontend = frontend;
	    	this.requestHost = requestHost;
	        this.requestPort = port;
	        this.requestNo = requestNo;
	        this.requestVal = requestVal;
	        
	        Runnable task3 = () -> {
	        	sendUDPRequest();
	        };
	        Thread thread3 = new Thread(task3);
	        thread3.start();
	        System.out.println("UDPSend called for " + frontend.currentReqID);
	        
	        //sendUDPRequest(); //This is threaded hence commenting this
	    }

	    //public String getResponseVal(){
	      //  return responseVal;
	    //}

	        public void sendUDPRequest(){
	        //This method will call the send to send the data
	            DatagramSocket aSocket=null;
	            try {
	                System.out.println("val of requestval "+requestVal);
	                System.out.println("val of requestHost "+requestHost);
	                System.out.println("val of requestPort "+requestPort);

	                DatagramPacket messageOut = new DatagramPacket(this.requestVal.getBytes(), this.requestVal.length(),this.requestHost,this.requestPort);
	                
	                ArrayList<String> attr = new ArrayList<String>(4);
	                
	                attr.add(0, "0"); //Adding initial state as failed
	                long starttime = System.nanoTime();
	                attr.add(1,null);
	                attr.add(2,String.valueOf(starttime));//Added start time of the request
	                attr.add(3,null);
	                
	                aSocket = new DatagramSocket();
	                
	                this.frontend.requestinfoReplica1.put(requestNo, attr);
	                this.frontend.requestinfoReplica2.put(requestNo, attr);
	                this.frontend.requestinfoReplica3.put(requestNo, attr);
	                System.out.println("Sending this message from UDP send "+this.requestVal + " on "+this.requestPort);
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
	}