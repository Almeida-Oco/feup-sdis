package controller.listener;

import network.Net_IO;
import network.PacketInfo;
import controller.Handler;
import controller.Pair;
import controller.ApplicationInfo;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;

public class Listener implements Runnable {
  Net_IO channel;
  ThreadPoolExecutor task_queue;
  static ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> > signals = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> >(4);


  public Listener(Net_IO channel, ThreadPoolExecutor tasks) {
    this.channel    = channel;
    this.task_queue = tasks;
  }

  @Override
  public void run() {
    String     txt = null;
    PacketInfo packet;

    System.out.println("Listening...");

    do {
      if ((packet = this.channel.recvMsg()) != null&& (packet.getSenderID() != ApplicationInfo.getServID())) {
        System.out.println("LOG: mc::recvMsg()\nTYPE = " + packet.getType() +
            "\nServ_ID = " + packet.getSenderID() + "\nCHUNK_N = " + packet.getChunkN());

        this.handleTask(Handler.newHandler(packet), packet);

        for (String key : signals.keySet()) {
          System.out.println("  KEY = '" + key + "'");
        }
      }
    } while (txt == null);

    this.task_queue.shutdown();
  }

  private void handleTask(Handler task, PacketInfo packet) {
    if (task != null) {
      this.registerForSignal(task);
      this.task_queue.execute((Runnable)task);
    }
    else {
      this.signal(packet);
    }
  }

  private void signal(PacketInfo packet) {
    String file_id = packet.getFileID() + "#" + packet.getChunkN();


    ConcurrentHashMap<String, Handler> chunks = this.signals.get(packet.getType());
    if (chunks != null) {
      System.out.println("Signalled ID '" + file_id + "'");
      Handler task = chunks.get(file_id);
      if (task != null) {
        task.signal(packet);
      }
    }
  }

  public Net_IO getChannel() {
    return this.channel;
  }

  // TODO no need to receive just the task, add remaining parameters
  public void registerForSignal(Handler task) {
    Pair<String, Handler> input = task.register();

    if (input != null) {
      ConcurrentHashMap<String, Handler> type = this.signals.computeIfAbsent(task.signalType(), (x)->{
        return new ConcurrentHashMap<String, Handler>();
      });
      if (type != null) {
        type.put(input.getFirst(), input.getSecond());
      }
      else {
        System.err.println("Why the frick is type null??");
      }
    }
  }

  public void removeFromSignal(Handler task) {
    Pair<String, Handler> input = task.register();
    if (input != null) {
      ConcurrentHashMap type = this.signals.get(task.signalType());
      if (type != null) {
        type.remove(input.getFirst());
      }
    }
  }
}
