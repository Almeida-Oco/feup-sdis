package controller;

import cli.User_IO;
import parser.ServerParser;
import controller.ApplicationInfo;
import controller.HandlerInterface;
import controller.client.Dispatcher;
import controller.listener.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;

class Server {
  private static final int cores     = Runtime.getRuntime().availableProcessors();
  private static final int MAX_TASKS = 100;
  private static final int RMI_PORT  = 1099;

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

    Listener mc_listener  = new Listener(ApplicationInfo.getMC(), task_queue);
    Listener mdb_listener = new Listener(ApplicationInfo.getMDB(), task_queue);
    Listener mdr_listener = new Listener(ApplicationInfo.getMDR(), task_queue);

    if (!registerClient(ApplicationInfo.getServID(), mc_listener, mdb_listener, mdr_listener)) {
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

  private static boolean registerClient(int id, Listener mc, Listener mdb, Listener mdr) {
    HandlerInterface stub;
    Registry         registry;

    try {
      stub     = (HandlerInterface)UnicastRemoteObject.exportObject(new Dispatcher(mc, mdb, mdr), 0);
      registry = LocateRegistry.getRegistry(RMI_PORT);
    }
    catch (RemoteException err) {
      System.err.println("Failed to register client handler!\n - " + err.getMessage());
      return false;
    }

    if (!tryBinding(registry, Integer.toString(id), stub)) {
      System.out.println("Creating registry...");
      try {
        registry = LocateRegistry.createRegistry(RMI_PORT);
      }
      catch (RemoteException err) {
        System.err.println("Failed to create registry!\n - " + err.getMessage());
        return false;
      }
    }

    return tryBinding(registry, Integer.toString(id), stub);
  }

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
