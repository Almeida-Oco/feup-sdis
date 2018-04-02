package controller;

import cli.User_IO;
import parser.ClientParser;
import controller.DispatcherInterface;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starter of the initiator-peer protocol
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class Client {
  private static final String BACKUP  = "BACKUP";
  private static final String RESTORE = "RESTORE";
  private static final String RECLAIM = "RECLAIM";
  private static final String DELETE  = "DELETE";
  private static final String STATE   = "STATE";
  private static final String CHECK   = "CHECK";

  /**
   * Main entry point for the initiator-peer protocl
   * @param args[] The command line arguments
   */
  public static void main(String args[]) {
    if (!ClientParser.parseArgs(args)) {
      User_IO.clientUsage();
      return;
    }
    DispatcherInterface handler = getObj(ClientParser.getIP(), ClientParser.getPort(), ClientParser.getName());
    if (handler == null) {
      return;
    }
    String protocol = args[1];

    try {
      if (protocol.equals(BACKUP)) {
        handler.backup(args[2], Integer.parseInt(args[3]));
      }
      else if (protocol.equals(RESTORE)) {
        handler.restore(args[2]);
      }
      else if (protocol.equals(RECLAIM)) {
        handler.reclaim(args[2]);
      }
      else if (protocol.equals(DELETE)) {
        handler.delete(args[2]);
      }
      else if (protocol.equals(STATE)) {
        handler.state();
      }
      else if (protocol.equals(CHECK)) {
        handler.check();
      }
      else {
        System.out.println("Unknown protocol '" + protocol + "'");
      }
    }
    catch (RemoteException err) {
      System.err.println("Failed to start protocol '" + protocol + "' due to RMI issues!\n - "
          + err.getMessage());
      err.printStackTrace();
      return;
    }
  }

  /**
   * Gets the RMI object from the Registry
   * @param  ip   IP to fetch object from
   * @param  port Port to use to fetch object
   * @param  name Name of object to fetch
   * @return      Instance of RMI object
   */
  private static DispatcherInterface getObj(String ip, int port, String name) {
    try {
      Registry registry = LocateRegistry.getRegistry(ip, port);
      if (registry == null) {
        System.err.println("Failed to get registry from " + ip + ":" + port);
        return null;
      }
      return (DispatcherInterface)registry.lookup(name);
    }
    catch (RemoteException err) {
      System.err.println("Could not get object '" + name + "' from RMI!\n - " + err.getMessage());
      return null;
    }
    catch (NotBoundException err) {
      System.err.println("'" + name + "' is a non existing RMI object!\n - " + err.getMessage());
      return null;
    }
  }
}
