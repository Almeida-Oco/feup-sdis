package controller.listener;

import network.Net_IO;
import controller.Pair;
import network.PacketInfo;
import controller.Handler;
import controller.ApplicationInfo;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

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

    do {
      if ((packet = this.channel.recvMsg()) != null&& (packet.getSenderID() != ApplicationInfo.getServID())) {
        this.handleTask(Handler.newHandler(packet), packet);
      }
    } while (txt == null);

    this.task_queue.shutdown();
  }

  private void handleTask(Handler task, PacketInfo packet) {
    if (task != null) {
      this.registerForSignal(task.register());
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
      Handler task = chunks.get(file_id);
      if (task != null) {
        task.signal(packet);
      }
    }
  }

  public boolean sendMsg(PacketInfo packet) {
    return this.channel.sendMsg(packet);
  }

  public PacketInfo recvMsg(PacketInfo packet) {
    return this.channel.recvMsg();
  }

  public InetAddress getAddr() {
    return this.channel.getAddr();
  }

  public int getPort() {
    return this.channel.getPort();
  }

  public void registerForSignal(Pair<String, Handler> task) {
    if (task != null) {
      ConcurrentHashMap<String, Handler> type = this.signals.computeIfAbsent(task.second().signalType(), (x)->{
        return new ConcurrentHashMap<String, Handler>();
      });
      if (type != null) {
        type.put(task.first(), task.second());
      }
      else {
        System.err.println("Why the frick is type null??");
      }
    }
  }

  public void registerForSignal(String chunk_id, String signal_type, Handler task) {
    if (chunk_id != null&& signal_type != null&& task != null) {
      ConcurrentHashMap<String, Handler> type = this.signals.computeIfAbsent(signal_type, (x)->{
        return new ConcurrentHashMap<String, Handler>();
      });
      if (type != null) {
        type.put(chunk_id, task);
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
        type.remove(input.first());
      }
    }
  }
}
