package files;

import java.util.Vector;

public class FileChunk implements Comparable<FileChunk> {
  Vector<Integer> stored_in;
  byte[] chunk_data;
  int chunk_n;
  int chunk_size;
  int rep_degree;


  public FileChunk(byte[] data, int size, int chunk_n, int desired_rep, Vector<Integer> peers) {
    this.stored_in  = peers;
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = desired_rep;
  }

  public FileChunk(byte[] data, int size, int chunk_n, int desired_rep) {
    this.stored_in  = new Vector<Integer>();
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = desired_rep;
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

  static int binarySearch(Vector<FileChunk> chunks, int chunk_n) {
    int low = 0, high = chunks.size(), mid, number;

    while (low <= high) {
      mid    = (low + high) / 2;
      number = chunks.get(mid).getChunkN();

      if (number > chunk_n) {
        high = mid - 1;
      }
      else if (number < chunk_n) {
        low = mid + 1;
      }
      else {
        return mid;
      }
    }

    return -1;
  }
}
