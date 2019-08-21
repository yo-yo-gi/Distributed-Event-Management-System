package src.Server;

public interface ServerInterface {
	String BookEvent (String CustID, String EventID, String EventType) throws Exception;
	  String GetBookingSchedule (String CustID, boolean Internal) throws Exception;
	  String CancelEvent (String CustID, String EventID, String EventType) throws Exception;
	  String AddEvent (String CustID, String EventID, String EventType, int Capacity) throws Exception;
	  String RemoveEvent (String EventID, String EventType) throws Exception;
	  String ListEventAvailability (String CustID, String EventType, boolean Internal) throws Exception;
	  String SwapEvent (String CustID, String NewEventID, String NewEventType, String OldEventID, String OldEventType, boolean internal) throws Exception;
	 
}