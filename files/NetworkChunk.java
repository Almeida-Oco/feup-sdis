package files;

import java.util.Vector;

/**
 * Holds information about a single chunk stored in the network
 * @author Gonçalo Moreno
 * @author João Almeida
 */
class NetworkChunk extends Chunk {
  /**
   * Initializes a new {@link NetworkChunk}
   * @param data        The data of the chunk
   * @param size        Size of the data of the chunk
   * @param chunk_n     Number of the chunk
   * @param desired_rep Desired replication degre
   */
  NetworkChunk(int chunk_n, int desired_rep, Integer peer_id) {
    super(chunk_n, desired_rep);
    if (peer_id != null) {
      super.addPeer(peer_id);
    }
  }

  LocalChunk toLocalChunk(byte[] data, int size, int peer_id) {
    LocalChunk chunk = new LocalChunk(data, size, this.chunk_n, this.desired_rep, peer_id);

    for (Integer replicator : this.replicators) {
      chunk.addPeer(replicator);
    }

    return chunk;
  }
}
