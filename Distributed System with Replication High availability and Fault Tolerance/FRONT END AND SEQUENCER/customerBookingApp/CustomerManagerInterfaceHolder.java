package customerBookingApp;

/**
* customerBookingApp/CustomerManagerInterfaceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CustomerManager.idl
* Friday, 2 August, 2019 12:23:53 AM EDT
*/

public final class CustomerManagerInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public customerBookingApp.CustomerManagerInterface value = null;

  public CustomerManagerInterfaceHolder ()
  {
  }

  public CustomerManagerInterfaceHolder (customerBookingApp.CustomerManagerInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = customerBookingApp.CustomerManagerInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    customerBookingApp.CustomerManagerInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return customerBookingApp.CustomerManagerInterfaceHelper.type ();
  }

}
