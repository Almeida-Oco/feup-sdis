package controller.listener;

import network.*;
import controller.server.Handler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class MCListener implements Runnable {
  Net_IO mc, mdr, mdb;
  ThreadPoolExecutor task_queue;
  ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> > signals;

  public MCListener(Net_IO mc, Net_IO mdb, Net_IO mdr, ThreadPoolExecutor queue) {
    this.mc         = mc;
    this.mdr        = mdr;
    this.mdb        = mdb;
    this.task_queue = queue;
    this.signals    = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> >(4);
  }

  public void run() {
    String     txt = null;
    PacketInfo packet;

    System.out.println("Listening...");

    do {
      if ((packet = this.mc.recvMsg()) != null) {
        System.out.println("LOG: mc::recvMsg() -> " + packet.getType());

        this.handleTask(Handler.newHandler(packet, mc, mdr, mdb));
      }
    } while (txt == null || !txt.equals("EXIT"));

    this.task_queue.shutdown();
  }

  private void handleTask(Handler task) {
    if (task != null) {
      this.registerForSignal(task);
      this.task_queue.execute(task);
    }
    else {
      this.signal(packet.getType(), packet.getFileID(), packet.getChunkN());
    }
  }

  private void signal(String type, String file_name, int chunk_n) {
    String file_id = file_name + "#" + chunk_n;

    ConcurrentHashMap<String, Handler> chunks = this.signals.get(type);
    if (chunks != null) {
      Handler task = chunks.get(file_id);
      if (task != null) {
        task.signal(file_id);
      }
    }
  }

  private void registerForSignal(Handler task) {
    Pair<String, Handler> input = task.register();
    if (input != null) {
      this.signals.put(task.signalType, input);
    }
  }
}
