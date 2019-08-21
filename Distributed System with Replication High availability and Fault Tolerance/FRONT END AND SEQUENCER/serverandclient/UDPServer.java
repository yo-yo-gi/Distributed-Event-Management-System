package serverandclient;

//package comp6231.a1;

/*Initially instance of this class will be instantiated
Which will run the UDPreceive method, which loops through receive of UDP

*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer { //implements Runnable{

    ServerImpl serverinstance; //Created during starting the server
    int portNo; //Port for this server
    public static Thread receiveThread=null;
    private volatile static boolean running;
    DatagramSocket aSocket = null;
    
    
    UDPServer(ServerImpl s, int portNo){
        this.serverinstance = s;
        this.portNo=portNo;
        //Calling receive method which will always listen on portNo
        Runnable task = () -> {
            UDPreceive(portNo);
        };
        Thread receiveThread = new Thread(task);
        receiveThread.start();
        System.out.println("Starting UDP server on "+portNo +" for "+s.getNameOfCity());
    }

        private void UDPreceive(int receivePort) {
            //DatagramSocket aSocket = null;
            
            try {
            	running=true;
                aSocket = new DatagramSocket(receivePort);
                byte[] buffer = new byte[2048];
                System.out.println("Server "+receivePort+" Started............");
                //while (true) {
                while (running) {
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                    aSocket.receive(request);

                    System.out.println("Received data from "+new String(request.getData()));
                    UDPResponse resp = new UDPResponse(this.serverinstance,request); //UDPResponse will unpack the DatagramPacket and send the response back
                    Thread respThread = new Thread(resp);
                    respThread.start();
                    buffer = new byte[2048];
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



        public void stopServer() {
   		 if (receiveThread == null) return;
            running = false;
            aSocket.close();
   	}
        
        


}