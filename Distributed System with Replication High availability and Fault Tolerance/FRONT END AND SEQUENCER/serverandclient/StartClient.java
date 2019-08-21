//mayank
package serverandclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import customerBookingApp.CustomerManagerInterface;
import customerBookingApp.CustomerManagerInterfaceHelper;

public class StartClient {

	    String customerID;
	    char custType;

	    StartClient(String custID) {
	        this.customerID = custID;
	        //this.custType=this.customerID.substring(3,1);   
	        //PropertyConfigurator.configure("G:\\Mayank\\Summer 19\\COMP 6231\\Assignment 1\\Solution\\log4j_ds.properties");
	        

	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
	        System.setProperty("input_file_name", ".//clientLogs//"+this.customerID+ "_"+ dateFormat.format(new Date()));
	    }

	    public static void main(String[] args) {
	        
	        Scanner sc = new Scanner(System.in);
	        String id = null;
	        while (true) {
	            System.out.println("Please enter your user ID: ");
	            id = sc.nextLine().toUpperCase();

	            if ((!id.substring(0, 3).equals("MTL") && !id.substring(0, 3).equals("TOR") && !id.substring(0, 3).equals("OTW")) || id.length() != 8) {
	                //All of them are invalid case hence exit the system
	                System.out.println("Invalid authentication.. Try again..");
	                //logger.info("Invalid authentication.. Try again..");
	                continue;
	            }
	            else{
	                break;
	            }
	        }

	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
	        System.setProperty("input_file_name", ".//clientLogs//"+id+ "_"+ dateFormat.format(new Date()));
	        Logger logger = LogManager.getLogger(StartClient.class);
	        PropertyConfigurator.configure("G:\\Final_Project\\Final_Project\\log4j_ds.properties");
	        
	        while (true)
	        {
	            if (id.substring(3, 4).equals("M"))
	            {
	                //Manager
//	                System.out.println("Authentication successful");
	                System.out.println("Please enter your choice from the following: ");
	                System.out.println("1 for addEvent");
	                System.out.println("2 for removeEvent");
	                System.out.println("3 for listEventAvailability");
	                System.out.println("4 for bookEvent");
	                System.out.println("5 for getBookingSchedule");
	                System.out.println("6 for cancelEvent");
	                System.out.println("7 for swapEvent");
	                System.out.println("8 for normal EXIT");

	                String actionInput = sc.nextLine();
	                if (actionInput.equals("8")) {
	                    System.out.println("Exiting the system ....");
	                    logger.info("Exiting the system ....");
	                    System.exit(0);
	                }
	                else if (!actionInput.equals("1") && !actionInput.equals("2") && !actionInput.equals("3") &&
	                        !actionInput.equals("4") && !actionInput.equals("5")&& !actionInput.equals("7")
	                        && !actionInput.equals("6")) {
	                    while (true) {
	                        System.out.println("Invalid input.. Please try again");
	                        System.out.println("Please enter your choice from the following: ");

	                        System.out.println("1 for addEvent");
	                        System.out.println("2 for removeEvent");
	                        System.out.println("3 for listEventAvailability");
	                        System.out.println("4 for bookEvent");
	                        System.out.println("5 for getBookingSchedule");
	                        System.out.println("6 for cancelEvent");
	                        System.out.println("7 for swapEvent");
	    	                System.out.println("8 for normal EXIT");
	                        
	                        actionInput = sc.nextLine();
	                        System.out.println("Value of actionInput is " + actionInput);
	                        if (actionInput.equals("1") || actionInput.equals("2") || actionInput.equals("3")) {
	                            break;
	                        } else if (actionInput.equals("4")) {
	                            System.out.println("Exiting the system .....");
	                            logger.info("Exiting the system .....");
	                            System.exit(0);
	                        }
	                    }
	                }

	                CustomerManagerInterface custManagerobj =  null;
	               // if(id.substring(0,3).equals("MTL")){

	                	try {
	                    
	                    ORB orb = ORB.init(args, null);
	        			//-ORBInitialPort 1050 -ORBInitialHost localhost
	        			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	        			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	        			 custManagerobj = (CustomerManagerInterface) CustomerManagerInterfaceHelper.narrow(ncRef.resolve_str("frontend"));
	                	}
	                	catch (Exception e) {
	            			System.out.println("Client exception: " + e);
	            			e.printStackTrace();
	            		}
	                    
	                //}


	                //Now we got correct action input hence moving further
	                if (actionInput.equals("1")) {//Addevent
	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 ||(!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                            	System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                continue;

	                            } 
	                            
	                            else if(!eID.substring(0,3).equalsIgnoreCase(id.substring(0,3))) {
	                            	
	                            	System.out.println(eID +" can not be added on "+id.substring(0,3) + " server");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                continue;
	                            	
	                            }
	                            
	                            else {
	                                break;
	                            }
	                        }


	                        System.out.println("Enter the " + eID + " event's capacity: ");
	                        String capacity = sc.nextLine();
	                        while (true) {
	                            if (Integer.parseInt(capacity) <= 0) {
	                                //Validate the event id MTLE100519
	                            	System.out.println("Capacity can not be less than equal to zero!");
	                            	System.out.println("Enter the " + eID + " event's capacity: ");
	    	                        capacity = sc.nextLine();
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }
	                        
	                        

	                        //Calling addEvent
	                        System.out.println("Calling addevent ...");
	                        logger.info("Calling addevent ...");
	                        String rAddEvent = custManagerobj.addEvent(id, eID, eType, capacity);
	                        //int rAddEvent = eManager.removeEvent(eID, eType);
	                        System.out.println(rAddEvent);
	                      /*  if (rAddEvent.toLowerCase().contains("success")) {
	                            System.out.println("addEvent: Requested by manager " + id + " has been successfully added to " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("addEvent: Requested by manager " + id + " has been successfully added to " + id.substring(0, 3).toUpperCase() + " server");
	                            
	                        } else {
	                            System.out.println("addEvent: Error!! Requested by manager " + id + " failed on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("addEvent: Error!! Requested by manager " + id + " failed on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if (actionInput.equals("2")) {//RemoveEvent

	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                continue;

	                            }
	                            
	                            
	                            else if(!eID.substring(0,3).equalsIgnoreCase(id.substring(0,3))) {
	                            	
	                            	System.out.println(eID +" can not be added on "+id.substring(0,3) + " server");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                continue;
	                            	
	                            }
	                            else {
	                                break;
	                            }
	                        }

	                        //Calling addEvent
	                        System.out.println("Calling removeEvent ...");
	                        logger.info("Calling removeEvent ...");
	                        String rRemoveEvent = custManagerobj.removeEvent(id, eID, eType);
	                        //int rAddEvent = eManager.removeEvent(eID, eType);
	                        System.out.println(rRemoveEvent);
/*	                        if (rRemoveEvent.toLowerCase().contains("success")) {
	                            System.out.println("removeEvent: Requested by manager " + id + " has been removed from " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("removeEvent: Requested by manager " + id + " has been removed from " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("removeEvent: Error!! Requested by manager " + id + " failed on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("removeEvent: Error!! Requested by manager " + id + " failed on " + id.substring(0, 3).toUpperCase() + " server");
	                        }
*/
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }

	                }

	                else if (actionInput.equals("3")) {//listEventavailability
	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Calling listAvailability ...");
	                        logger.info("Calling listAvailability ...");
	                        String rListEventAvailability = custManagerobj.listEventAvailability(id, eType);
	                        System.out.println(rListEventAvailability);
	                        if (rListEventAvailability.toLowerCase().contains("success")) {
	                            
	                            System.out.println(rListEventAvailability);
	                            //logger.info("listEventAvailability: Requested by manager " + id + " has been successfully executed on " + id.substring(0, 3).toUpperCase() + " server");
	                            //logger.info(rListEventAvailability);
	                        } else {
	                            System.out.println("listEventAvailability: Error!! Requested by manager " + id + " execution failed on " + id.substring(0, 3).toUpperCase() + " server");
	                            //logger.error("listEventAvailability: Error!! Requested by manager " + id + " execution failed on " + id.substring(0, 3).toUpperCase() + " server");
	                        }

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if(actionInput.equals("4")){//bookEvent
	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }

	                        System.out.println("Enter the customer ID: ");
	                        String cID = sc.nextLine();
	                        while (true) {
	                            if (cID.length() != 8 || (!cID.substring(0, 3).equals("MTL") && !cID.substring(0,3).equals("TOR") && !cID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid customer id... Try again");
	                                System.out.println("Enter the customer ID: ");
	                                cID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }

	                        //Calling addEvent
	                        System.out.println("Calling bookevent ...");
	                        logger.info("Calling bookevent ...");
	                        String rBookEvent = custManagerobj.bookEvent(id, cID, eID, eType);
	                        System.out.println(rBookEvent);
	                        /*if (rBookEvent.toLowerCase().contains("success")) {
	                            System.out.println("bookEvent: Requested by manager " + id + " has successfully booked event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("bookEvent: Requested by manager " + id + " has successfully booked event on " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("bookEvent: Error!! Requested by manager " + id + " failed to book event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("bookEvent: Error!! Requested by manager " + id + " failed to book event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if(actionInput.equals("5")){
	                    try {

	                        System.out.println("Enter the customer ID: ");
	                        String cID = sc.nextLine();
	                        while (true) {
	                            if (cID.length() != 8 || (!cID.substring(0, 3).equals("MTL") && !cID.substring(0,3).equals("TOR") && !cID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid customer id... Try again");
	                                logger.error("Invalid customer id... Try again");
	                                System.out.println("Enter the customer ID: ");
	                                cID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }

	                        System.out.println("Calling getBookingSchedule ...");
	                        logger.info("Calling getBookingSchedule ...");
	                        String rGetBookingSchedule = custManagerobj.getBookingSchedule(id, cID);
	                        System.out.println(rGetBookingSchedule);
	                        /*if (rGetBookingSchedule.toLowerCase().contains("success")) {
	                            System.out.println("getBookingSchedule: Below is the booking schedule requested by " + id + " for " +cID+" on " + id.substring(0, 3).toUpperCase() + " server");
	                            System.out.println(rGetBookingSchedule);
	                            
	                            logger.info("getBookingSchedule: Below is the booking schedule requested by " + id + " for " +cID+" on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info(rGetBookingSchedule);
	                            
	                        } else {
	                            System.out.println("getBookingSchedule: Error!! Booking schedule executed by " + id + " for " + cID +" failed on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("getBookingSchedule: Error!! Booking schedule executed by " + id + " for " + cID +" failed on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if(actionInput.equals("6")){

	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }

	                        System.out.println("Enter the customer ID: ");
	                        String cID = sc.nextLine();
	                        while (true) {
	                            if (cID.length() != 8 || (!cID.substring(0, 3).equals("MTL") && !cID.substring(0,3).equals("TOR") && !cID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid customer id... Try again");
	                                System.out.println("Enter the customer ID: ");
	                                cID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }
	                        //Calling cancelEvent
	                        System.out.println("Calling cancelEvent ...");
	                        logger.info("Calling cancelEvent ...");
	                        String rCancelEvent = custManagerobj.cancelEvent(id, cID, eID, eType);
	                        System.out.print(rCancelEvent);
	                        //int rAddEvent = eManager.removeEvent(eID, eType);
	                        /*if (rCancelEvent.toLowerCase().contains("success")) {
	                            System.out.println("cancelEvent: Requested by manager " + id + " has successfully canceled event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("cancelEvent: Requested by manager " + id + " has successfully canceled event on " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("cancelEvent: Error!! Requested by manager " + id + " failed to cancel event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("cancelEvent: Error!! Requested by manager " + id + " failed to cancel event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }

	                }
	                
	                else if(actionInput.equals("7")){//swapevent

	                    try {
	                        System.out.println("Enter the new event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid new event type... Try again");
	                                System.out.println("Enter the new event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the new eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid new event id... Try again");
	                                System.out.println("Enter the new eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }
	                        
	                        System.out.println("Enter the old event type: ");
	                        String oldeType = sc.nextLine();
	                        while (true) {
	                            if (!oldeType.toLowerCase().equals("conferences") && !oldeType.toLowerCase().equals("trade shows")
	                                    && !oldeType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid old event type... Try again");
	                                System.out.println("Enter the old event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the old eventID: ");
	                        String oldeID = sc.nextLine();
	                        while (true) {
	                            if (oldeID.length() != 10 || (!oldeID.substring(0, 3).equals("MTL") && !oldeID.substring(0,3).equals("TOR") && !oldeID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid old event id... Try again");
	                                System.out.println("Enter the old eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }   
	                        System.out.println("Enter the customer ID: ");
	                        String cID = sc.nextLine();
	                        while (true) {
	                            if (cID.length() != 8 || (!cID.substring(0, 3).equals("MTL") && !cID.substring(0,3).equals("TOR") && !cID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid customer id... Try again");
	                                System.out.println("Enter the customer ID: ");
	                                cID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }
	                        //Calling swapevent
	                        System.out.println("Calling swapEvent ...");
	                        logger.info("Calling swapEvent ...");
	                        //String rCancelEvent = custManagerobj.cancelEvent(cID, eID, eType);
	                        String rSwapEvent = custManagerobj.swapEvent(id, cID, eID, eType, oldeID, oldeType);
	                        System.out.println(rSwapEvent);
	                        //int rSwapEvent = eManager.swapEvent(cID, eID, eType, oldeID, oldeType);
	                        /*if (rSwapEvent.toLowerCase().contains("success")) {
	                            System.out.println("swapEvent: Requested by manager " + id + " has successfully swapped event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("swapEvent: Requested by manager " + id + " has successfully swapped event on " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("swapEvent: Error!! Requested by manager " + id + " failed to swap event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("swapEvent: Error!! Requested by manager " + id + " failed to swap event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	                }

	            else if (id.substring(3, 4).equals("C")) {//Customer

	                System.out.println("Please enter your choice from the following: ");

	                System.out.println("1 for bookEvent");
	                System.out.println("2 for getBookingSchedule");
	                System.out.println("3 for cancelEvent");
	                System.out.println("4 for swapEvent");
	                System.out.println("5 for normal EXIT");
	                

	                String actionInput = sc.nextLine();

	                if (actionInput.equals("5")) {
	                    System.out.println("Exiting the system ....");
	                    System.exit(0);
	                }
	                else if (!actionInput.equals("1") && !actionInput.equals("2") && !actionInput.equals("3") && !actionInput.equals("4"))
	                {
	                    while (true) {
	                        System.out.println("Invalid input.. Please try again");
	                        System.out.println("Please enter your choice from the following: ");
	                        System.out.println("1 for addEvent");
	                        System.out.println("2 for removeEvent");
	                        System.out.println("3 for listEventAvailability");
	                        System.out.println("4 for swapEvent");
	                        System.out.println("5 for normal EXIT");
	                        
	                        
	                        actionInput = sc.nextLine();
	                        System.out.println("Value of actionInput is " + actionInput);
	                        if (actionInput.equals("1") || actionInput.equals("2") || actionInput.equals("3") || actionInput.equals("4")) {
	                            break;
	                        } else if (actionInput.equals("5")) {
	                            System.out.println("Exiting the system .....");
	                            System.exit(0);
	                        }
	                    }
	                }

	                CustomerManagerInterface customerObj =null; 
	               // if(id.substring(0,3).equals("MTL")){
	                	try {
	                	ORB orb = ORB.init(args, null);
	        			//-ORBInitialPort 1050 -ORBInitialHost localhost
	        			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
	        			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	        			 customerObj = (CustomerManagerInterface) CustomerManagerInterfaceHelper.narrow(ncRef.resolve_str("frontend"));
	                	}
	                	catch (Exception e) {
	            			System.out.println("Client exception: " + e);
	            			e.printStackTrace();
	            		}


	                if(actionInput.equals("1")){
	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }

	                        System.out.println("Calling bookevent ...");
	                        logger.info("Calling bookevent ...");
	                        String rBookEvent = customerObj.bookEvent(id, id, eID, eType);
	                        System.out.print(rBookEvent);
	                        //int rAddEvent = eManager.removeEvent(eID, eType);
	                        /*if (rBookEvent.toLowerCase().contains("success")) {
	                            System.out.println("bookEvent: Requested by customer " + id + " has successfully booked event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("bookEvent: Requested by customer " + id + " has successfully booked event on " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("bookEvent: Error!! Requested by customer " + id + " failed to book event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("bookEvent: Error!! Requested by customer " + id + " failed to book event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if(actionInput.equals("2")){
	                    try {

	                        System.out.println("Calling getBookingSchedule ...");
	                        String rGetBookingSchedule = customerObj.getBookingSchedule(id, id);
	                        System.out.print(rGetBookingSchedule);
	                        /*if (rGetBookingSchedule.toLowerCase().contains("success")) {
	                            System.out.println("getBookingSchedule: Below is the booking schedule requested by " + id + " for " +id+" on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("getBookingSchedule: Below is the booking schedule requested by " + id + " for " +id+" on " + id.substring(0, 3).toUpperCase() + " server");
	                            System.out.println(rGetBookingSchedule);
	                            logger.info(rGetBookingSchedule);
	                        } else {
	                            System.out.println("getBookingSchedule: Error!! Booking schedule executed by " + id + " for " + id +" failed on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("getBookingSchedule: Error!! Booking schedule executed by " + id + " for " + id +" failed on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }

	                else if(actionInput.equals("4")){//swapevent

	                    try {
	                        System.out.println("Enter the new event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid new event type... Try again");
	                                System.out.println("Enter the new event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the new eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid new event id... Try again");
	                                System.out.println("Enter the new eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }
        
	                        System.out.println("Enter the old event type: ");
	                        String oldeType = sc.nextLine();
	                        while (true) {
	                            if (!oldeType.toLowerCase().equals("conferences") && !oldeType.toLowerCase().equals("trade shows")
	                                    && !oldeType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid old event type... Try again");
	                                System.out.println("Enter the old event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the old eventID: ");
	                        String oldeID = sc.nextLine();
	                        while (true) {
	                            if (oldeID.length() != 10 || (!oldeID.substring(0, 3).equals("MTL") && !oldeID.substring(0,3).equals("TOR") && !oldeID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid old event id... Try again");
	                                System.out.println("Enter the old eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;

	                            } else {
	                                break;
	                            }
	                        }
	                        
	                        //Calling swapevent
	                        System.out.println("Calling swapEvent ...");
	                        logger.info("Calling swapEvent ...");
	                        String rSwapEvent = customerObj.swapEvent(id, id, eID, eType, oldeID, oldeType);
	                        System.out.print(rSwapEvent);
	                        /*if (rSwapEvent.toLowerCase().contains("success")) {
	                            System.out.println("swapEvent: Requested by customer " + id + " has successfully swapped event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("swapEvent: Requested by customer " + id + " has successfully swapped event on " + id.substring(0, 3).toUpperCase() + " server");
	                        } else {
	                            System.out.println("swapEvent: Error!! Requested by customer " + id + " failed to swap event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("swapEvent: Error!! Requested by customer " + id + " failed to swap event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }

	                }
	                
	                
	                else if(actionInput.equals("3")){

	                    try {
	                        System.out.println("Enter the event type: ");
	                        String eType = sc.nextLine();
	                        while (true) {
	                            if (!eType.toLowerCase().equals("conferences") && !eType.toLowerCase().equals("trade shows")
	                                    && !eType.toLowerCase().equals("seminars")) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event type... Try again");
	                                System.out.println("Enter the event type: ");
	                                eType = sc.nextLine(); //Have not validated the event ID yet
	                            } else {
	                                break; //Correct event type has been collected
	                            }
	                        }

	                        System.out.println("Enter the eventID: ");
	                        String eID = sc.nextLine();
	                        while (true) {
	                            if (eID.length() != 10 || (!eID.substring(0, 3).equals("MTL") && !eID.substring(0,3).equals("TOR") && !eID.substring(0,3).equals("OTW"))) {
	                                //Validate the event id MTLE100519
	                                System.out.println("Invalid event id... Try again");
	                                System.out.println("Enter the eventID: ");
	                                eID = sc.nextLine(); //Have not validated the event ID yet
	                                break;
	                            } else {
	                                break;
	                            }
	                        }
	                        


	                        //Calling cancelEvent
	                        System.out.println("Calling cancelEvent ...");
	                        String rCancelEvent = customerObj.cancelEvent(id, id, eID, eType);
	                        System.out.println(rCancelEvent);
	                       /* if (rCancelEvent.toLowerCase().contains("success")) {
	                            System.out.println("cancelEvent: Requested by manager " + id + " has successfully canceled event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.info("cancelEvent: Requested by manager " + id + " has successfully canceled event on " + id.substring(0, 3).toUpperCase() + " server");
	                            
	                        } else {
	                            System.out.println("cancelEvent: Error!! Requested by manager " + id + " failed to cancel event on " + id.substring(0, 3).toUpperCase() + " server");
	                            logger.error("cancelEvent: Error!! Requested by manager " + id + " failed to cancel event on " + id.substring(0, 3).toUpperCase() + " server");
	                        }*/

	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                }
	            }
	        }
	    }
}