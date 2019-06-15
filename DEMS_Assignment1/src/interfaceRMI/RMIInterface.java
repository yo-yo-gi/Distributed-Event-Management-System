package interfaceRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Yogesh Nimbhorkar
 *
 */
public interface RMIInterface extends Remote {
	
	
	//Manager methods Start
	
	public String addEvent(String eventId ,String eventType, int bookingCapacity) throws RemoteException;
	
	public String removeEvent (String managerId, String eventId) throws RemoteException;
	
	public String listEventAvailability(String eventType) throws RemoteException;
	
	//Manager methods End
	
	//User methods Start
	
	public String bookEvent(String customerId, String eventId, String eventType) throws RemoteException;
	
	public String getBookingSchedule(String customerId) throws RemoteException;
	
	public String cancelEvent (String customerId,String eventType, String eventId) throws RemoteException;
	
	// User methods End
	
	
	
	
}
