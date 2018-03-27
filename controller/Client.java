package controller;

import cli.User_IO;
import parser.ClientParser;
import controller.HandlerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

class Client {
  private static final String BACKUP  = "BACKUP";
  private static final String RESTORE = "RESTORE";
  private static final String RECLAIM = "RECLAIM";
  private static final String DELETE  = "DELETE";
  private static final String STATE   = "STATE";

  public static void main(String args[]) {
    if (!ClientParser.parseArgs(args)) {
      User_IO.clientUsage();
      return;
    }
    HandlerInterface handler = getObj(ClientParser.getIP(), ClientParser.getPort(), ClientParser.getName());
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
    }
    catch (RemoteException err) {
      System.out.println("Failed to start protocol '" + protocol + "' due to RMI issues!\n - " + err.getMessage());
      return;
    }
  }

  private static HandlerInterface getObj(String ip, int port, String name) {
    try {
      Registry registry = LocateRegistry.getRegistry(ip, port);
      if (registry == null) {
        System.err.println("Failed to get registry from " + ip + ":" + port);
        return null;
      }
      HandlerInterface handler = (HandlerInterface)registry.lookup(name);
      return handler;
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
