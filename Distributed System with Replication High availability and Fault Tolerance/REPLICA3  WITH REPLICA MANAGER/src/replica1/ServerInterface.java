/**
 * @author Yogesh Nimbhorkar
 */
package replica1;

/**
 * @author Yogesh Nimbhorkar
 *
 */
public interface ServerInterface {
	
	
	//Manager methods Start
	
	public String addEvent(String eventId ,String eventType, int bookingCapacity);
	
	public String removeEvent (String managerId, String eventId);
	
	public String listEventAvailability(String eventType)  ;
	
	//Manager methods End
	
	//User methods Start
	
	public String bookEvent(String customerId, String eventId, String eventType)  ;
	
	public String getBookingSchedule(String customerId)  ;
	
	public String cancelEvent (String customerId,String eventType, String eventId)  ;

	String swapEvent(String customerId, String newEventId, String newEventType, String oldEventId, String oldEventType);
	
	// User methods End
	
	
	
	
}