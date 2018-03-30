package files;

import java.util.Vector;

/**
 * Holds information about a single chunk
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class FileChunk implements Comparable<FileChunk> {
  /** Peers that replicated this chunk */
  Vector<Integer> stored_in;

  /** Data of the chunk */
  byte[] chunk_data;

  /** Number of the chunk */
  int chunk_n;

  /** Size of the data of the chunk */
  int chunk_size;

  /** Desired replication degree */
  int rep_degree;

  /**
   * Initializes a new {@link FileChunk}
   * @param data        The data of the chunk
   * @param size        Size of the data of the chunk
   * @param chunk_n     Number of the chunk
   * @param desired_rep Desired replication degre
   * @param peers       Peers that already stored the chunk
   */
  public FileChunk(byte[] data, int size, int chunk_n, int desired_rep, Vector<Integer> peers) {
    this.stored_in  = peers;
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = desired_rep;
  }

  /**
   * Initializes a new {@link FileChunk}
   * @param data        The data of the chunk
   * @param size        Size of the data of the chunk
   * @param chunk_n     Number of the chunk
   * @param desired_rep Desired replication degre
   */
  public FileChunk(byte[] data, int size, int chunk_n, int desired_rep) {
    this.stored_in  = new Vector<Integer>();
    this.chunk_data = data;
    this.chunk_size = size;
    this.chunk_n    = chunk_n;
    this.rep_degree = desired_rep;
  }

  /**
   * Adds a new peer to {@link FileChunk#stored_in}
   * @param peer_id Peer ID
   */
  public void addPeer(int peer_id) {
    synchronized (this) {
      if (!this.stored_in.contains(peer_id)) {
        this.stored_in.add(peer_id);
      }
    }
  }

  public void removePeer(Integer peer_id) {
    synchronized (this) {
      this.stored_in.remove(peer_id);
    }
  }

  /**
   * Gets the chunk data
   * @return {@link FileChunk#chunk_data}
   */
  public byte[] getData() {
    return this.chunk_data;
  }

  /**
   * Gets the chunk desired replication degree
   * @return {@link FileChunk#rep_degree}
   */
  public int getDesiredRep() {
    return this.rep_degree;
  }

  /**
   * Gets the chunk actual replication degree
   * @return Actual replication degree
   */
  public int getActualRep() {
    synchronized (this) {
      return this.stored_in.size();
    }
  }

  /**
   * Gets the replicators of the chunk
   * @return {@link FileChunk#stored_in}
   */
  public Vector<Integer> getReplicators() {
    return this.stored_in;
  }

  /**
   * Gets the size of the chunk data
   * @return {@link FileChunk#chunk_size}
   */
  public int getSize() {
    return this.chunk_size;
  }

  /**
   * Gets the number of the chunk
   * @return {@link FileChunk#chunk_n}
   */
  public int getChunkN() {
    return this.chunk_n;
  }

  /**
   * Checks if the chunk is the last chunk of the file
   * @return Whether the chunk is the final chunk of the file or not
   */
  public boolean isFinalChunk() {
    return this.chunk_size < File_IO.MAX_CHUNK_SIZE;
  }

  @Override
  public int compareTo(FileChunk chunk) {
    return Integer.compare(this.chunk_n, ((FileChunk)chunk).chunk_n);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileChunk) {
      FileChunk chunk = (FileChunk)obj;
      return chunk.chunk_n == this.chunk_n;
    }
    return false;
  }

  /**
   * Binary searches a chunk by chunk number
   * @param  chunks  Vector of chunks (chunks need to be of same file)
   * @param  chunk_n Number of chunk to fin
   * @return         Index of the chunk, -1 if not found
   */
  static int binarySearch(Vector<FileChunk> chunks, int chunk_n) {
    int low = 0, high = chunks.size() - 1, mid, number;

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
