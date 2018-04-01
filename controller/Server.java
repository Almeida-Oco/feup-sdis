package controller;

import cli.User_IO;
import files.FileHandler;
import network.Net_IO;
import parser.ServerParser;
import controller.ApplicationInfo;
import controller.client.Dispatcher;
import controller.DispatcherInterface;
import controller.client.CheckHandler;

import java.util.Vector;
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
  private static Registry reg;

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
  private static DispatcherInterface startProgram() {
    Net_IO mc_channel  = ApplicationInfo.getMC();
    Net_IO mdb_channel = ApplicationInfo.getMDB();
    Net_IO mdr_channel = ApplicationInfo.getMDR();

    DispatcherInterface stub;

    if ((stub = registerClient(ApplicationInfo.getServID(), mc_channel, mdb_channel, mdr_channel)) == null) {
      return null;
    }


    Thread mc          = new Thread(new ChannelListener(mc_channel));
    Thread mdb         = new Thread(new ChannelListener(mdb_channel));
    Thread mdr         = new Thread(new ChannelListener(mdr_channel));
    Thread sig_handler = new Thread(new SignalHandler());
    mc.start();
    mdb.start();
    mdr.start();
    sig_handler.start();

    Vector<Pair<String, Integer> > reused_chunks = FileHandler.setup();
    if (ApplicationInfo.getVersion() >= 20) {
      CheckHandler check = new CheckHandler();
      check.start(reused_chunks, ApplicationInfo.getMC());
    }

    System.out.println("Ready!");
    try {
      sig_handler.join();
      mc.join();
      mdb.join();
      mdr.join();
    }
    catch (InterruptedException err) {
      System.err.println("Failed to join thread!\n - " + err.getMessage());
    }

    return stub;
  }

  /**
   * Registers the RMI object in the Registry
   * @param  id  ID of the object to register
   * @param  mc  MC {@link ChannelListener}
   * @param  mdb MDB {@link ChannelListener}
   * @param  mdr MDR {@link ChannelListener}
   * @return     The registered object, null on error
   */
  private static DispatcherInterface registerClient(int id, Net_IO mc, Net_IO mdb, Net_IO mdr) {
    DispatcherInterface stub;

    try {
      stub     = UnicastRemoteObject.exportObject(new Dispatcher(mc, mdb, mdr), 0);
      this.reg = LocateRegistry.getRegistry(ApplicationInfo.getAP());
    }
    catch (RemoteException err) {
      System.err.println("Failed to register client handler!\n - " + err.getMessage());
      return null;
    }

    if (!tryBinding(Integer.toString(id), stub)) {
      System.out.println("Creating registry...");
      try {
        this.reg = LocateRegistry.createRegistry(ApplicationInfo.getAP());
      }
      catch (RemoteException err) {
        System.err.println("Failed to create registry!\n - " + err.getMessage());
        return null;
      }
    }
    else {
      return stub;
    }

    if (tryBinding(Integer.toString(id), stub)) {
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
  private static boolean tryBinding(String id, DispatcherInterface stub) {
    try {
      this.reg.rebind(id, stub);
      return true;
    }
    catch (RemoteException err) {
      return false;
    }
  }
}
