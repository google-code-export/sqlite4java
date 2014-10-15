package com.almworks.sqlite4java_checker;

import com.almworks.sqlite4java.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceEvent;

import java.io.File;
import java.io.IOException;

/**
 * http://felix.apache.org/site/apache-felix-tutorial-example-1.html
 * Apache Felix Tutorial Example
 *
 * This class implements a simple bundle that utilizes the OSGi
 * framework's event mechanism to listen for service events. Upon
 * receiving a service event, it prints out the event's details.
 **/
public class Activator implements BundleActivator, ServiceListener {
  /**
   * Implements BundleActivator.start(). Prints
   * a message and adds itself to the bundle context as a service
   * listener.
   * @param context the framework context for the bundle.
   **/
  public void start(BundleContext context)
  {
    System.out.println("Starting to listen for service events.");

    sqlite4javaAction();

    context.addServiceListener(this);
  }

  private void sqlite4javaAction() {
    System.out.println("im here!");
    File outputFile;
    try {
      outputFile = File.createTempFile("prefix", "extension");
    } catch (IOException ex) {
      System.out.println("fail create temp file =(");
      return;
    }

    File dbFile = outputFile;
    SQLiteConnection db = new SQLiteConnection(dbFile);

    try {
      db.open(true);
      SQLiteStatement st = db.prepare("SELECT 'vaka vaka vaka'");
      try {
        st.step();
        String elem = st.columnString(0);
        System.out.println(elem);
      } finally {
        st.dispose();
      }
    } catch (SQLiteException ex) {
      System.out.println("sqlite4java broken =(\n" + ex.toString());
      ex.printStackTrace();
    }
  }

  /**
   * Implements BundleActivator.stop(). Prints
   * a message and removes itself from the bundle context as a
   * service listener.
   * @param context the framework context for the bundle.
   **/
  public void stop(BundleContext context)
  {
    context.removeServiceListener(this);
    System.out.println("Stopped listening for service events.");

    // Note: It is not required that we remove the listener here,
    // since the framework will do it automatically anyway.
  }

  /**
   * Implements ServiceListener.serviceChanged().
   * Prints the details of any service event from the framework.
   * @param event the fired service event.
   **/
  public void serviceChanged(ServiceEvent event)
  {
    String[] objectClass = (String[])
        event.getServiceReference().getProperty("objectClass");

    if (event.getType() == ServiceEvent.REGISTERED) {
      System.out.println(
          "Ex1: Service of type " + objectClass[0] + " registered.");
    }
    else if (event.getType() == ServiceEvent.UNREGISTERING) {
      System.out.println(
          "Ex1: Service of type " + objectClass[0] + " unregistered.");
    }
    else if (event.getType() == ServiceEvent.MODIFIED) {
      System.out.println(
          "Ex1: Service of type " + objectClass[0] + " modified.");
    }
  }
}
