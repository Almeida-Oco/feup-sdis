package controller;

import parser.ServerParser;
import cli.User_IO;
import network.Net_IO;
import controller.client.HandlerInterface;
import controller.client.Handler;
import controller.ApplicationInfo;
import controller.listener.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class Server {
  private static final int cores     = Runtime.getRuntime().availableProcessors();
  private static final int MAX_TASKS = 100;

  public static void main(String[] args) {
    if (!ServerParser.parseArgs(args)) {
      User_IO.serverUsage();
      return;
    }

    startProgram();
  }

  private static void startProgram() {
    LinkedBlockingQueue<Runnable> queue      = new LinkedBlockingQueue<Runnable>(MAX_TASKS);
    ThreadPoolExecutor            task_queue = new ThreadPoolExecutor(cores - 1, cores - 1, 0, TimeUnit.SECONDS, queue);

    for (int i = 2; i < Runtime.getRuntime().availableProcessors(); i++) {
      task_queue.prestartCoreThread();
    }

    Net_IO      mc = ApplicationInfo.getMC(), mdb = ApplicationInfo.getMDB(), mdr = ApplicationInfo.getMDR();
    MCListener  mc_listener  = new MCListener(mc, mdb, mdr, task_queue);
    MDBListener mdb_listener = new MDBListener(mc, mdb, mdr, task_queue);

    if (!registerClient(ApplicationInfo.getServID(), mc, mdb, mdr)) {
      return;
    }

    mc_listener.run();
    mdb_listener.run();
  }

  private static boolean registerClient(int id, Net_IO mc, Net_IO mdb, Net_IO mdr) {
    try {
      System.out.println("Registering: " + id);
      Registry         registry = LocateRegistry.createRegistry(8000);
      Handler          handler  = new Handler(mc, mdb, mdr);
      HandlerInterface stub     = (HandlerInterface)UnicastRemoteObject.exportObject(handler, 8080);

      System.out.println("IM HERE");
      registry.rebind("" + id, stub);
    }
    catch (RemoteException err) {
      System.err.println("Failed to register client handler!\n - " + err.getMessage());
      return false;
    }

    return true;
  }
}
