package controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;

class SignalCounter {
  ConcurrentHashMap<String, int> signal_counter;
  int max_count;

  public SignalCounter(int max) {
    this.signal_counter = new ConcurrentHashMap<String, int>();
    this.max_count      = max;
  }

  public void registerValue(String file_name, int chunk_n) {
    String name = file_name + "#" + chunk_n;

    this.signal_counter.put(name, 0);
  }

  public void signalValue(String file_name, int chunk_n) {
    boolean remove = false;
    String  name   = file_name + "#" + chunk_n;

    this.signal_counter.computeIfPresent(name, (key, value) - > {
      value += 1;
      remove = (value >= this.max_count);
    });

    if (remove) {
      this.signal_counter.remove(name);
    }
  }

  public Vector<Pair<String, int> > getRemainder() {
    Enumeration<String>        keys   = this.signal_counter.keys();
    Vector<Pair<String, int> > chunks = new Vector<Pair<String, int> >();

    while (keys.hasMoreElements()) {
      String name = keys.nextElement();
      int    hash = name.lastIndexOf('#');
      chunks.add(new Pair(name.substring(0, hash), Integer.parseInt(name.substring(hash))));
    }

    return chunks;
  }
}
