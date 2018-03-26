package files;

import java.util.Vector;

public class FileChunk implements Comparable<FileChunk> {
  Vector<Integer> stored_in;
  byte[] chunk_data;
  int chunk_n;
  int chunk_size;
  int rep_degree;


  FileChunk(byte[] data, int size, int chunk_n, int degree) {
    this.stored_in  = new Vector<Integer>();
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = degree;
  }

  public FileChunk(byte[] data, int size, int chunk_n) {
    this.stored_in  = new Vector<Integer>();
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = 1;
  }

  public void addPeer(int peer_id) {
    synchronized (this) {
      this.stored_in.add(peer_id);
    }
  }

  public byte[] getData() {
    return this.chunk_data;
  }

  public int getDesiredRep() {
    return this.rep_degree;
  }

  public int getActualRep() {
    return this.stored_in.size();
  }

  public int getSize() {
    return this.chunk_size;
  }

  public int getChunkN() {
    return this.chunk_n;
  }

  public boolean isFinalChunk() {
    return this.chunk_size < File_IO.MAX_CHUNK_SIZE;
  }

  @Override
  public int compareTo(FileChunk chunk) {
    return Integer.compare(this.chunk_n, ((FileChunk)chunk).chunk_n);
  }
}
