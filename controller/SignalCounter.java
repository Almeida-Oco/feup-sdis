package controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Vector;
import java.util.Enumeration;

class SignalCounter {
  ConcurrentHashMap<String, Integer> signal_counter = new ConcurrentHashMap<String, Integer>();
  int max_count;

  public SignalCounter(int max) {
    this.max_count = max;
  }

  public void registerValue(String file_name, int chunk_n) {
    String name = file_name + "#" + chunk_n;

    this.signal_counter.put(name, new Integer(0));
  }

  public void signalValue(String file_name, int chunk_n) {
    AtomicBoolean remove = new AtomicBoolean(false);
    String        name   = file_name + "#" + chunk_n;

    this.signal_counter.computeIfPresent(name, (key, value)->{
      remove.set(value >= this.max_count);
      return value + 1;
    });

    if (remove.get()) {
      this.signal_counter.remove(name);
    }
  }

  public Vector<Pair<String, Integer> > getRemainder() {
    Enumeration<String>            keys   = this.signal_counter.keys();
    Vector<Pair<String, Integer> > chunks = new Vector<Pair<String, Integer> >();

    while (keys.hasMoreElements()) {
      String name = keys.nextElement();
      int    hash = name.lastIndexOf('#');

      chunks.addElement(new Pair<String, Integer>(name.substring(0, hash), Integer.parseInt(name.substring(hash))));
    }

    return chunks;
  }
}
