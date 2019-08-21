package serverandclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class Multicast {



		Multicast(int receiveport){
	        Runnable task3 = () -> {
	            UDPreceive(receiveport);
	        };
	        Thread thread3 = new Thread(task3);
	        thread3.start();
	       		
		}
		
		
        private void UDPreceive(int receivePort) {
            DatagramSocket aSocket = null;
            try {
                aSocket = new DatagramSocket(receivePort);
                byte[] buffer = new byte[2048];
                System.out.println("Multicast receive server Started on " +receivePort+" ............");
                while (true) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);
                    String data = new String(request.getData()).trim();
                    System.out.println("Received this request at Multicast-> "+data);
                    udp(data);
                    
                    //System.out.println("Received data from "+new String(request.getData()));
                    String response = new String(request.getData());
                    buffer = new byte[2048];
                }
            }
            catch (SocketException e) {
            	System.out.println("Socket: " + e.getMessage());
            } catch (IOException e) {
            	System.out.println("IO: " + e.getMessage());
            	
            } finally {
            	if (aSocket != null)
            		aSocket.close();
            }
            
        }
		public static void udp(String message) {
			DatagramSocket aSocket = null;
			DatagramSocket bSocket = null;
			
			try {
				aSocket = new DatagramSocket();
				
//				requestID++;
				//byte[] m = (message +"_" + String.valueOf(requestID)).getBytes();
				byte[] m = message.getBytes();
				//InetAddress aHost = InetAddress.getByName("230.0.0.2");
				InetAddress aHost = InetAddress.getByName("230.1.1.5");

				DatagramPacket request = new DatagramPacket(m, m.length, aHost, 8001);
				aSocket.send(request);
				
				System.out.println("Mulicast complete");
				
				/*
				
				InetAddress a = InetAddress.getByName("132.205.64.118");
				DatagramPacket messageOut = new DatagramPacket("Hi".getBytes(), "Hi".length(),a,9001);
				bSocket = new DatagramSocket();
				bSocket.send(messageOut);
				*/
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public static void main(String[] args) throws Exception {
			new Multicast(4555); //This is the port where Multicast will receive from front end	
		}
	}
	
	



