package files;

import java.util.Vector;
import java.util.Collections;

/**
 * Holds information about an abstract chunk
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public abstract class Chunk implements Comparable<Integer> {
  /** Peers that replicated the chunk */
  Vector<Integer> replicators;

  /** Data of the chunk */
  byte[] data;

  /** Number of the chunk */
  int chunk_n;

  /** Size of the data of the chunk */
  int size;

  /** Desired replication degree */
  int desired_rep;

  /**
   * Initializes a new {@link Chunk}
   * @param chunk_n     Number of the chunk
   * @param desired_rep Desired replication degree of chunk
   */
  public Chunk(int chunk_n, int desired_rep) {
    this.replicators = new Vector<Integer>(desired_rep);
    this.chunk_n     = chunk_n;
    this.data        = null;
    this.size        = 0;
    this.desired_rep = desired_rep;
  }

  /**
   * Adds a new peer to {@link Chunk#replicators}
   * @param peer_id Peer ID
   */
  public void addPeer(Integer peer_id) {
    if (peer_id != null) {
      synchronized (this.replicators) {
        int index = Collections.binarySearch(this.replicators, peer_id);
        if (index < 0) {
          this.replicators.add((-(index) - 1), peer_id);
        }
      }
    }
  }

  /**
   * Removes a peer from {@link Chunk#replicators}
   * @param peer_id [description]
   */
  public void removePeer(Integer peer_id) {
    synchronized (this.replicators) {
      int index = Collections.binarySearch(this.replicators, peer_id);
      if (index > 0) {
        this.replicators.remove(index);
      }
    }
  }

  /**
   * Gets the chunk data
   * @return {@link FileChunk#data}
   */
  public byte[] getData() {
    return this.data;
  }

  /**
   * Gets the chunk desired replication degree
   * @return {@link FileChunk#desired_rep}
   */
  public int getDesiredRep() {
    return this.desired_rep;
  }

  /**
   * Gets the chunk actual replication degree
   * @return Actual replication degree
   */
  public int getActualRep() {
    synchronized (this.replicators) {
      return this.replicators.size();
    }
  }

  /**
   * Gets the size of the chunk data
   * @return {@link FileChunk#size}
   */
  public int getSize() {
    return this.size;
  }

  /**
   * Gets the replicators of the chunk
   * @return {@link FileChunk#replicators}
   */
  public Vector<Integer> getReplicators() {
    return this.replicators;
  }

  /**
   * Gets the number of the chunk
   * @return {@link FileChunk#chunk_n}
   */
  public int getChunkN() {
    return this.chunk_n;
  }

  /**
   * Sets the replication degree
   * @param rep_degree Desired replication degree
   */
  public void setRepDegree(int rep_degree) {
    this.desired_rep = rep_degree;
  }

  /**
   * Checks if the chunk is the last chunk of the file
   * @return Whether the chunk is the final chunk of the file or not
   */
  public boolean isFinalChunk() {
    return this.size < File_IO.MAX_CHUNK_SIZE && this.data != null;
  }

  @Override
  public int compareTo(Integer chunk_n) {
    return Integer.compare(this.chunk_n, chunk_n);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Chunk) {
      Chunk chunk = (Chunk)obj;
      return chunk.chunk_n == this.chunk_n;
    }
    return false;
  }
}
