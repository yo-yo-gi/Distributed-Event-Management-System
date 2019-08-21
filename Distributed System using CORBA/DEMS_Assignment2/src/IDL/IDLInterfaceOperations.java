package IDL;


/**
* IDL/IDLInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from C:/Users/yoges/Desktop/DSWorkspace/DS_Demo_Workspace/DEMS_Assignment2/src/idlinterface/IDLInterface.idl
* Monday, July 8, 2019 4:09:53 PM EDT
*/

public interface IDLInterfaceOperations 
{
  String addEvent (String eventId, String eventType, int bookingCapacity);
  String removeEvent (String managerId, String eventId);
  String listEventAvailability (String eventType);
  String bookEvent (String customerId, String eventId, String eventType);
  String getBookingSchedule (String customerId);
  String cancelEvent (String customerId, String eventType, String eventId);
  String swapEvent (String customerId, String newEventId, String newEventType, String oldEventId, String oldEventType);
} // interface IDLInterfaceOperations
