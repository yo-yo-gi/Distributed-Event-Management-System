package serverandclient;

//package comp6231.a1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/*This class' run method will have a send and receive functionality of UDP protocol
This class has an instance of server class to call the methods of any class.
 */
public class UDPRequest implements Runnable {

    InetAddress requestHost;
    int requestPort;
    String requestVal;
    String responseVal; //This will be a final response to be used by calling serverImpl to get the response.



    UDPRequest(InetAddress requestHost, int port, String requestVal){
        this.requestHost = requestHost;
        this.requestPort = port;
        this.requestVal = requestVal;
        this.responseVal="";
    }

    public String getResponseVal(){
        return responseVal;
    }

        public void run(){
        //This method will call the send and receive to send and receive the data
            DatagramSocket aSocket=null;
            try {
                InetAddress requestHost = InetAddress.getByName("localhost");
                System.out.println("val of requestval "+requestVal);
                System.out.println("val of requestHost "+requestHost);
                System.out.println("val of requestval "+requestPort);

                DatagramPacket messageOut = new DatagramPacket(this.requestVal.getBytes(), this.requestVal.length(),requestHost,this.requestPort);
                aSocket = new DatagramSocket();
                aSocket.send(messageOut);

                byte[] inResponse = new byte[2048];
                DatagramPacket messageIn = new DatagramPacket(inResponse, inResponse.length);
                System.out.println("Next will be receive ");
                aSocket.receive(messageIn); //Received the message
                System.out.println("Waiting for receive ");
                //Set the value of response to this.responseVal and the task is complete. This var will be consumed on serverImpl side
                this.responseVal = new String(messageIn.getData()).trim();
                System.out.println("Received response is "+this.responseVal);

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
