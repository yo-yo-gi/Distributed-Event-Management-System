package serverandclient;

//package comp6231.a1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

//This will have socket.send
//Actual methods will be called here

public class UDPResponse extends Thread {

    ServerImpl targetServer;
    DatagramPacket inrequest;
    DatagramPacket outresponse;

    UDPResponse(ServerImpl targetServer, DatagramPacket inrequest){
        this.targetServer = targetServer;
        this.inrequest=inrequest;
    }

    public void run(){

        callTheCorrespondingMethod();

        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            //byte[] buffer = new byte[2048];
                //DatagramPacket reply = new DatagramPacket(request.getData(), request.getLength(), request.getAddress(), request.getPort());// reply packet ready
                aSocket.send(this.outresponse);// reply sent

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

    }
    public void callTheCorrespondingMethod(){
    //Extract the information from the Datagram packet

        byte[] inMessage = this.inrequest.getData();
        int outPort = this.inrequest.getPort();
        InetAddress outServerAddress = this.inrequest.getAddress();

        String packedData = new String(inMessage);
        packedData = packedData.trim(); //Trimming the data to avoid the appended character
        //Now unpack all the information from the incoming packedData and call the respective method
System.out.println("Value of packeddata is "+packedData);
        String[] unpackedData = packedData.split("&");
        String executeMethod = unpackedData[0];
        String paramsForMethod = unpackedData[1]; //This is delimited by ,

        if(executeMethod.equals("innerlistEventAvailability")){
            //Call innerlistEventAvailability on serverinstance ans return the result
            String eType = paramsForMethod;
            System.out.println("calling innerlistEventAvailability with "+eType +" value");
            String responseFromMethod = this.targetServer.innerlistEventAvailability(eType);
            this.outresponse = new DatagramPacket(responseFromMethod.getBytes(),responseFromMethod.length(),outServerAddress,outPort);
        }


        else if(executeMethod.equals("innerbookEvent")){
            System.out.println("Inside innerbookEvent");
            String[] inParams = paramsForMethod.split(",");
            String cID=inParams[0];
            String eID=inParams[1];
            String eType=inParams[2];
            //String responseFromMethod = Integer.toString(this.targetServer.innerbookEvent(cID,eID,eType));//Change
            String responseFromMethod = this.targetServer.innerbookEvent(cID,eID,eType);
            System.out.println("Response from innerbookevent method is "+responseFromMethod);
            this.outresponse = new DatagramPacket(responseFromMethod.getBytes(),responseFromMethod.length(),outServerAddress,outPort);
        }

        else if(executeMethod.equals("cancelEvent")){
            System.out.println("Inside cancelEvent");
            String[] inParams = paramsForMethod.split(",");
            String cID=inParams[0];
            String eID=inParams[1];
            String eType=inParams[2];
            //String responseFromMethod = Integer.toString(this.targetServer.cancelEvent(cID,eID,eType));//Change
            String responseFromMethod = this.targetServer.cancelEvent(cID,eID,eType);
            System.out.println("Response from cancel method is "+responseFromMethod);
            this.outresponse = new DatagramPacket(responseFromMethod.getBytes(),responseFromMethod.length(),outServerAddress,outPort);
        }

        else if(executeMethod.equals("innergetBookingSchedule")){
            System.out.println("Inside innergetBookingSchedule");

            String cID=paramsForMethod;
            System.out.println("Value of CID is "+cID);
            System.out.println("Calling getBookingSchedule on "+this.targetServer.getNameOfCity() + " "+cID);
            String responseFromMethod = this.targetServer.innergetBookingSchedule(cID);
            System.out.println("Response from getBookingSchedule method is "+responseFromMethod);
            this.outresponse = new DatagramPacket(responseFromMethod.getBytes(),responseFromMethod.length(),outServerAddress,outPort);
        }

        else{
            System.out.println("Method name is wrong!" );
        }
    }
}
