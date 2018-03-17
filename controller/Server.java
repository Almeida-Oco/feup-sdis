package controller;

import parser.ArgParser;
import cli.User_IO;
import controller.server.Handler;
import controller.ApplicationInfo;
import controller.listener.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class Server {
  private static final int cores               = Runtime.getRuntime().availableProcessors();
  private static final int MAX_TASKS           = 100;
  private static final Pattern channel_pattern =
    Pattern.compile(" *((?<ip1>\\d{1,4}).(?<ip2>\\d{1,4}).(?<ip3>\\d{1,4}).(?<ip4>\\d{1,4}))?:?(?<port>\\d{1,7}) *");

  public static void main(String[] args) {
    if (!argsValid(args) || !ArgParser.parseArgs(args)) {
      return;
    }

    startProgram();
  }

  private static boolean argsValid(String[] args) {
    if (args.length != 6) {
      return User_IO.printUsage();
    }

    return true;
  }

  private static void startProgram() {
    LinkedBlockingQueue<Runnable> queue      = new LinkedBlockingQueue<Runnable>(MAX_TASKS);
    ThreadPoolExecutor            task_queue = new ThreadPoolExecutor(cores - 1, cores - 1, 0, TimeUnit.SECONDS, queue);

    for (int i = 2; i < Runtime.getRuntime().availableProcessors(); i++) {
      task_queue.prestartCoreThread();
    }

    MCListener mc_listener = new MCListener(ApplicationInfo.getMC(),
                                            ApplicationInfo.getMDB(),
                                            ApplicationInfo.getMDR(),
                                            task_queue);
    MDBListener mdb_listener = new MDBListener(ApplicationInfo.getMC(),
                                               ApplicationInfo.getMDB(),
                                               ApplicationInfo.getMDR(),
                                               task_queue);

    mc_listener.run();
    mdb_listener.run();
  }
}
