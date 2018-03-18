package controller;

import parser.ServerParser;
import cli.User_IO;
import controller.client.HandlerInterface;
import controller.client.Dispatcher;
import controller.ApplicationInfo;
import controller.listener.Listener;

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
    if (!ServerParser.parseArgs(args)){
      User_IO.serverUsage();
      return;
    }

    startProgram();
  }

  private static void startProgram() {
    LinkedBlockingQueue<Runnable> queue      = new LinkedBlockingQueue<Runnable>(MAX_TASKS);
    ThreadPoolExecutor            task_queue = new ThreadPoolExecutor(cores - 1, cores - 1, 0, TimeUnit.SECONDS, queue);

    for (int i = 2; i < Runtime.getRuntime().availableProcessors(); i++){
      task_queue.prestartCoreThread();
    }

    Listener mc_listener  = new Listener(ApplicationInfo.getMC(), task_queue);
    Listener mdb_listener = new Listener(ApplicationInfo.getMDB(), task_queue);
    Listener mdr_listener = new Listener(ApplicationInfo.getMDR(), task_queue);

    if (!registerClient(ApplicationInfo.getServID(), mc_listener, mdb_listener, mdr_listener)){
      return;
    }

    Thread mc  = new Thread(mc_listener);
    Thread mdb = new Thread(mdb_listener);
    mc.start();
    mdb.start();

    try {
      mc.join();
      mdb.join();
    }
    catch (InterruptedException err){
      System.err.println("Failed to join thread!\n - " + err.getMessage());
    }
  }

  private static boolean registerClient(int id, Listener mc, Listener mdb, Listener mdr) {
    try {
      Registry         registry = LocateRegistry.createRegistry(ApplicationInfo.getAP());
      Dispatcher       handler  = new Dispatcher(mc, mdb, mdr);
      HandlerInterface stub     = (HandlerInterface)UnicastRemoteObject.exportObject(handler, ApplicationInfo.getAP());

      registry.rebind("" + id, stub);
    }
    catch (RemoteException err){
      System.err.println("Failed to register client handler!\n - " + err.getMessage());
      return false;
    }

    return true;
  }
}
