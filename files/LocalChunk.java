package files;

/**
 * Holds information about a single chunk stored locally
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class LocalChunk extends Chunk {
  /**
   * Initializes a new {@link LocalChunk}
   * @param data        The data of the chunk
   * @param size        Size of the data of the chunk
   * @param chunk_n     Number of the chunk
   * @param desired_rep Desired replication degre
   */
  LocalChunk(byte[] data, int size, int chunk_n, int desired_rep, int peer_id) {
    super(chunk_n, desired_rep);
    this.data = data;
    this.size = size;
    super.addPeer(peer_id);
  }

  LocalChunk(byte[] data, int size, int chunk_n, int desired_rep) {
    super(chunk_n, desired_rep);
    this.data = data;
    this.size = size;
  }
}
