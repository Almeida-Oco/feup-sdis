package controller.listener;

import network.Net_IO;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ConcurrentHashMap;

abstract class Listener extends Runnable {
  Net_IO channel;
  ThreadPoolExecutor task_queue;
  static ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> > signals = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler> >(4);


  public abstract void run();

  protected abstract void handleTask(Handler task);

  protected abstract void signal(String type, String file_name, int chunk_n);

  public void registerForSignal(Handler task) {
    Pair<String, Handler> input = task.register();
    if (input != null) {
      ConcurrentHashMap type = this.signals.get(task.signalType());
      if (type != null) {
        type.put(input.getFirst(), input.getSecond());
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
