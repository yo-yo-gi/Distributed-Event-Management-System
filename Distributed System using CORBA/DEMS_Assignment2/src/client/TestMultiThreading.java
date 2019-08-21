/**
 * 
 */
package client;

import java.util.concurrent.BrokenBarrierException;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import IDL.IDLInterface;
import IDL.IDLInterfaceHelper;

/**
 * @author Yogesh Nimbhorkar
 *
 */
public class TestMultiThreading {
	public static void main(String[] args) {
		TestClient client = new TestClient(args);
		try {
			client.testmultithreading();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}

}
class TestClient {
	ORB orb;
	org.omg.CORBA.Object objRef;
	static NamingContextExt ncRef;
	static IDLInterface torStub, ottStub, montStub;

	public TestClient(String[] args) {
		try {
			orb = ORB.init(args, null);
			objRef = orb.resolve_initial_references("NameService");
			ncRef = NamingContextExtHelper.narrow(objRef);
			torStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("torontoStub"));
			ottStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("ottawaStub"));
			montStub = (IDLInterface) IDLInterfaceHelper.narrow(ncRef.resolve_str("montrealStub"));

		} catch (Exception e) {
			System.out.println("Client exception: " + e);
			e.printStackTrace();
		}
	}

	public void testmultithreading() throws InterruptedException, BrokenBarrierException {



		
		//Exchange event
		Runnable first = () -> {
			Thread.currentThread().setName("Thread 1:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Toronto:  "
					+ torStub.swapEvent("TORC2345", "TORA100519", "TRADE SHOWS", "TORA100519", "CONFERENCES"));

		};
		
		//Exchange event
		Runnable second = () -> {
			Thread.currentThread().setName("Thread 2:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Ottawa:  "
					+ ottStub.swapEvent("OTWC2345", "OTWA100519", "TRADE SHOWS", "OTWA100519", "CONFERENCES"));

		};
		
		//Exchange Event
		Runnable third = () -> {
			Thread.currentThread().setName("Thread 3:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ montStub.swapEvent("MTLC2345",  "MTLA100519", "TRADE SHOWS", "MTLA100519", "CONFERENCES"));

		};
		

		// Book Event
		Runnable fourth = () -> {
			Thread.currentThread().setName("Thread 4:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Toronto:  "
					+ torStub.bookEvent("TORC2345", "TRADE SHOWS", "TORE100519"));

		};
		
		// Book Event
		Runnable fifth = () -> {
			Thread.currentThread().setName("Thread 5:");
			System.out.println(Thread.currentThread().getName() + "     " + "Ottawa:  "
					+ ottStub.bookEvent("OTWC2345", "TRADE SHOWS", "OTWE100519"));

		};
		
		// Book Event
		Runnable sixth = () -> {
			Thread.currentThread().setName("Thread 6:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ montStub.bookEvent("MTLC2345", "TRADE SHOWS", "MTLE100519"));

		};

//		// get Booking Schedule
		Runnable seventh = () -> {
			Thread.currentThread().setName("Thread 7:");
			System.out.println();
			System.out.println(Thread.currentThread().getName() + "     " + "Montreal:  "
					+ montStub.getBookingSchedule("MTLC2345"));

		};
		
		

		Thread thread1 = new Thread(first);
		Thread thread2 = new Thread(second);
		Thread thread3 = new Thread(third);
		Thread thread4 = new Thread(fourth);
		Thread thread5 = new Thread(fifth);
		Thread thread6 = new Thread(sixth);
		Thread thread7 = new Thread(seventh);
		thread1.start();
		thread3.start();
		thread2.start();
		thread7.start();
		thread4.start();
		thread6.start();
		thread5.start();


	}

}
