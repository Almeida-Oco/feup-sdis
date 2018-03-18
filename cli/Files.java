package cli;

import controller.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class Files {
  ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer> > files;

  public Files() {
    this.files = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer> > ();
  }

  // TODO this can receive the numebr of chunks
  public void addNewFile(String file_name) {
    this.files.put(file_name, new ConcurrentHashMap<Integer, Integer>());
  }

  public void addChunk(String file_name, int chunk_n, int chunk_size) {
    this.files.get(file_name).put(chunk_n, chunk_size);
  }

  public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer> > getFiles() {
    return this.files;
  }
}
