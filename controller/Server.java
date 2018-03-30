package controller;

import cli.User_IO;
import parser.ServerParser;
import controller.ApplicationInfo;
import controller.HandlerInterface;
import controller.client.Dispatcher;
import controller.ChannelListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Starter of the peer protocol
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class Server {
  private static final int RMI_PORT = 1099;

  /**
   * Main entry point for the peer protocol
   * @param args [description]
   */
  public static void main(String[] args) {
    if (!ServerParser.parseArgs(args)) {
      User_IO.serverUsage();
      return;
    }

    startProgram();
  }

  /**
   * Starts the server, after the arguments were parsed
   */
  private static void startProgram() {
    HandlerInterface stub;
    ChannelListener  mc_listener = ApplicationInfo.getMC(),
        mdb_listener             = ApplicationInfo.getMDB(),
        mdr_listener             = ApplicationInfo.getMDR();

    if ((stub = registerClient(ApplicationInfo.getServID(), mc_listener, mdb_listener, mdr_listener)) == null) {
      return;
    }

    Thread mc  = new Thread(mc_listener);
    Thread mdb = new Thread(mdb_listener);
    Thread mdr = new Thread(mdr_listener);
    mc.start();
    mdb.start();
    mdr.start();
    System.out.println("Ready");
    try {
      mc.join();
      mdb.join();
      mdr.join();
    }
    catch (InterruptedException err) {
      System.err.println("Failed to join thread!\n - " + err.getMessage());
    }
  }

  /**
   * Registers the RMI object in the Registry
   * @param  id  ID of the object to register
   * @param  mc  MC {@link ChannelListener}
   * @param  mdb MDB {@link ChannelListener}
   * @param  mdr MDR {@link ChannelListener}
   * @return     The registered object, null on error
   */
  private static HandlerInterface registerClient(int id, ChannelListener mc, ChannelListener mdb, ChannelListener mdr) {
    HandlerInterface stub;
    Registry         registry;

    try {
      stub     = (HandlerInterface)UnicastRemoteObject.exportObject(new Dispatcher(mc, mdb, mdr), 0);
      registry = LocateRegistry.getRegistry(RMI_PORT);
    }
    catch (RemoteException err) {
      System.err.println("Failed to register client handler!\n - " + err.getMessage());
      return null;
    }

    if (!tryBinding(registry, Integer.toString(id), stub)) {
      System.out.println("Creating registry...");
      try {
        registry = LocateRegistry.createRegistry(RMI_PORT);
      }
      catch (RemoteException err) {
        System.err.println("Failed to create registry!\n - " + err.getMessage());
        return null;
      }
    }
    else {
      return stub;
    }

    if (tryBinding(registry, Integer.toString(id), stub)) {
      return stub;
    }
    return null;
  }

  /**
   * Tries to bind the ID to the registry
   * @param  reg  {@link Registry} to bind object to
   * @param  id   ID of object to be binded
   * @param  stub Object to be binded
   * @return      Whether it was successfully binded or not
   */
  private static boolean tryBinding(Registry reg, String id, HandlerInterface stub) {
    try {
      reg.rebind(id, stub);
      return true;
    }
    catch (RemoteException err) {
      return false;
    }
  }
}
