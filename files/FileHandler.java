package files;

import controller.Pair;
import controller.ApplicationInfo;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.io.IOException;
import java.util.Collections;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FileHandler {
  private final static int MAX_N_CHUNKS = 999999;

  /** Maximum space allocated to the program in bytes */
  private static AtomicInteger max_space = new AtomicInteger(8192000);  //Equivalent to 8MB

  /** Space used by the program in bytes */
  private static AtomicInteger used_space = new AtomicInteger(0);       //Equivalent to 8MB

  /**
   * Holds the local files which were sent for backup
   * Key is real path to file
   */
  private static ConcurrentHashMap<String, FileInfo> backed_files = new ConcurrentHashMap<String, FileInfo>();

  /**
   * Holds the chunks stored by the peer
   * Key is the ID of the file
   */
  private static ChunkStorer local_storer = new ChunkStorer();

  /**
   * Holds the chunks stored by the network
   * Key is the ID of the file
   */
  private static ChunkStorer network_storer = new ChunkStorer();

  public static void setup() {
    File_IO.setup(ApplicationInfo.getServID());
  }

  // ----- ADD METHODS --------

  /**
   * Stores a chunk in memory
   * @param  file_id     ID of the file
   * @param  chunk_n     Number of the chunk to store
   * @param  desired_rep Desired replication degree of chunk
   * @param  data        Data of the chunk
   * @param  data_size   Size of the chunk data
   * @return             Whether it was successfully stored or not
   * Uses a state machine, where the states are whether the chunk is present in {@link FileHandler#local_storer} or {@link FileHandler#network_storer}
   * STATE 1 (!in_local, !in_net) -> Stores chunk in memory and adds it to {@link FileHandler#local_storer}
   * STATE 2 (!in_local, in_net) -> Generates a {@link LocalChunk} using {@link NetworkChunk#toLocalChunk()}. Removes the chunk from network and adds it to local
   * No other state should happen in the protocol
   */
  public static boolean storeLocalChunk(String file_id, int chunk_n, int desired_rep, byte[] data, int data_size) {
    Chunk chunk;
    int   peer_id = ApplicationInfo.getServID();

    synchronized (network_storer) {
      if ((chunk = network_storer.getChunk(file_id, chunk_n)) == null) {
        chunk = new LocalChunk(data, data_size, chunk_n, desired_rep, peer_id);
      }
      else {
        chunk = ((NetworkChunk)chunk).toLocalChunk(data, data_size, peer_id);
        chunk.setRepDegree(desired_rep);
        network_storer.removeChunk(file_id, chunk_n);
      }
    }
    if (!File_IO.storeFile(file_id + "#" + chunk_n, data, data_size)) {
      return false;
    }
    return local_storer.addChunk(file_id, chunk);
  }

  /**
   * Tries to add a replicator peer to a network chunk
   * @param  file_id ID of the file
   * @param  chunk_n Number of the network chunk
   * @param  peer_id ID of peer that stored the file
   * @return         Whether the peer was successfully added or not
   * Uses a state machine, where the states are whether the chunk is present in {@link FileHandler#local_storer} or {@link FileHandler#network_storer}
   * STATE 1 (!in_local, !in_net) -> Creates a new {@link NetworkChunk} and adds it to network_storer
   * STATE 2 (!in_local, in_net) -> Adds the peer to the replicators of the {@link NetworkChunk}
   * STATE 3 (in_local, !in_net) -> Adds the peer to the replicators of the {@link LocalChunk}
   * Other states are not possible
   */
  public static boolean addNetworkPeer(String file_id, int chunk_n, int rep_degree, Integer peer_id) {
    Chunk chunk;

    synchronized (local_storer) {
      if ((chunk = local_storer.getChunk(file_id, chunk_n)) == null) {
        if ((chunk = network_storer.getChunk(file_id, chunk_n)) == null) {
          chunk = new NetworkChunk(chunk_n, rep_degree, peer_id);
          return network_storer.addChunk(file_id, chunk);
        }
      }
    }

    chunk.addPeer(peer_id);
    return true;
  }

  /**
   * Reads the file from the file system
   * @param  file_name  Name of file to read
   * @param  rep_degree Desired replication degree of file
   * @return            Information of file in {@link FileInfo}, null on error
   */
  public static FileInfo readFile(String file_name, int rep_degree) {
    FileInfo info = File_IO.readFile(file_name, rep_degree);

    if (info == null) {
      return null;
    }

    backed_files.put(file_name, info);
    return info;
  }

  /**
   * Restores a file based on the given chunks
   * @param  file_name Name of file to restore
   * @param  chunks    Chunks to use to restore file
   * @return           Whether the restore was successfull or not
   */
  public static boolean restoreFile(String file_name, byte[][] chunks) {
    return File_IO.restoreFile(file_name, chunks);
  }

  // ----- REMOVE METHODS ---------

  /**
   * Erases a previously backed up file
   * @param  file_name Name of file to erase
   * @return           Whether the file was successfully erased or not
   */
  public static boolean eraseBackedFile(String file_name) {
    backed_files.remove(file_name);
    return File_IO.eraseFile(file_name);
  }

  /**
   * Removes a local chunk from file-system
   * @param  file_id ID of file to remove
   * @param  chunk_n Number of chunk to remove
   * @return         Whether the chunk was removed or not
   */
  public static boolean remLocalChunk(String file_id, int chunk_n) {
    if (local_storer.removeChunk(file_id, chunk_n)) {
      return File_IO.eraseFile(file_id + "#" + chunk_n);
    }
    return false;
  }

  /**
   * Erases all chunks of a file
   * @param  file_id ID of file to delete the chunks
   * @return         Whether all chunks were successfully erased or not
   */
  public static boolean eraseFileChunks(String file_id) {
    Vector<Chunk> chunks = local_storer.getFileChunks(file_id);

    if (chunks == null) {
      return false;
    }

    chunks.forEach((chunk)->File_IO.eraseFile(file_id + "#" + chunk.getChunkN()));
    return local_storer.removeFile(file_id);
  }

  // ------ RECLAIM --------------

  /**
   * Reclaims the given amount of bytes from the protocol
   * @param  bytes Number of bytes to reclaim
   * @return       Vector with chunks to remove in order to reclaim the given amount of space
   * The algorith first retrieves all overly replicated chunks, if the reclaimed space is not enough it starts by removing one chunk of each stored file
   */
  public static Vector<Pair<String, Chunk> > reclaimSpace(long bytes) {
    Vector<Pair<String, Chunk> > rem_chunks = new Vector<Pair<String, Chunk> >();
    ConcurrentHashMap<String, Vector<Chunk> > local_chunks = local_storer.getChunks();
    long rem_bytes;
    synchronized (local_chunks) {
      rem_bytes = reclaimOverlyRep(local_chunks, rem_chunks, bytes);
      int size = local_chunks.size(), has_chunks = size;

      if (rem_bytes > 0) {
        for (int i = 0; i < MAX_N_CHUNKS && has_chunks > 0 && rem_bytes > 0; i++, has_chunks = size) {
          for (Map.Entry<String, Vector<Chunk> > entry : local_chunks.entrySet()) {
            if (i < entry.getValue().size()) {
              Chunk chunk = entry.getValue().get(i);
              if (chunk.getActualRep() <= chunk.getDesiredRep()) {
                rem_chunks.add(new Pair<String, Chunk>(entry.getKey(), chunk));
                rem_bytes -= chunk.getSize();
              }
            }
            else {
              has_chunks--;
            }
          }
        }
      }
    }

    if (rem_bytes < 0) {
      System.out.println("Reclaiming " + (bytes - rem_bytes) + " bytes!");
    }
    return rem_chunks;
  }

  /**
   * Reclaims space from the overly replicated chunks
   * @param  rem_chunks Vector to store the chunks to remove
   * @param  bytes      Number of bytes to reclaim
   * @return            Number of bytes reclaimed by the replicated chunks
   */
  private static long reclaimOverlyRep(ConcurrentHashMap<String, Vector<Chunk> > local_chunks, Vector<Pair<String, Chunk> > rem_chunks, long bytes) {
    long rem_bytes = bytes;

    int size = local_chunks.size(), has_chunks = size;

    for (int i = 0; i < MAX_N_CHUNKS && has_chunks > 0 && rem_bytes > 0; i++, has_chunks = size) {
      for (Map.Entry<String, Vector<Chunk> > entry : local_chunks.entrySet()) {
        if (i < entry.getValue().size()) {
          Chunk chunk = entry.getValue().get(i);

          if (chunk.getActualRep() > chunk.getDesiredRep()) {
            rem_chunks.add(new Pair<String, Chunk>(entry.getKey(), chunk));
            rem_bytes -= chunk.getSize();
          }
        }
        else {
          has_chunks--;
        }
      }
    }

    return rem_bytes;
  }

  // ------ GETTERS SETTERS ---------

  /**
   * Checks if file exists in backed up files table
   * @param  file_id File name
   * @return         Whether file was previously backed up or not
   */
  public static boolean isLocalFile(String file_id) {
    for (Map.Entry<String, FileInfo> entry : backed_files.entrySet()) {
      if (entry.getValue().getID().equals(file_id)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets information about a file from the table
   * @param  file_name Path to the file
   * @return           The information of the file, null if non-existing
   */
  public static FileInfo getBackedFile(String file_name) {
    if (file_name == null) {
      return null;
    }
    return backed_files.get(file_name);
  }

  /**
   * Gets the maximum allocated space to the protocol
   * @return {@link File_IO#max_space}
   */
  public static int getMaxSpace() {
    return max_space.get();
  }

  /**
   * Gets the space used by the protocol
   * @return {@link File_IO#used_space}
   */
  public static int getUsedSpace() {
    return used_space.get();
  }

  /**
   * Gets the remaining available space to the protocol
   * @return Number of bytes remaining
   */
  public static int getRemainingSpace() {
    int rem = max_space.get() - used_space.get();

    if (rem < 0) {
      return 0;
    }
    return rem;
  }

  /**
   * Gets a stored chunk from the {@link File_IO#local_chunks}
   * @param  file_id ID of file
   * @param  chunk_n Number of chunk
   * @return         {@link Chunk} or null if non-existing
   */
  public static Chunk getStoredChunk(String file_id, int chunk_n) {
    return local_storer.getChunk(file_id, chunk_n);
  }

  public static Chunk getBackedChunk(String file_id, int chunk_n) {
    FileInfo info = backed_files.get(file_id);

    if (info == null) {
      return null;
    }
    return info.getChunk(chunk_n);
  }

  /**
   * Gets the actual replication degree of a chunk
   * @param  file_id ID of file
   * @param  chunk_n Number of the chunk
   * @return         Replication degree of chunk, -1 if not found
   */
  public static int chunkRepDegree(String file_id, int chunk_n) {
    Chunk chunk = local_storer.getChunk(file_id, chunk_n);

    if (chunk != null) {
      return chunk.getActualRep();
    }
    return -1;
  }

  /**
   * Gets the table of files backed up by the network
   * @return {@link File_IO#backed_files}
   */
  public static ConcurrentHashMap<String, FileInfo> getBackedUpTable() {
    return backed_files;
  }

  /**
   * Gets the table of chunks stored by the peer
   * @return {@link File_IO#local_chunks}
   */
  public static ConcurrentHashMap<String, Vector<Chunk> > getChunksTable() {
    return local_storer.getChunks();
  }

  /**
   * Sets the maximum amount of bytes available to the service
   * @param max Number of bytes available
   */
  public static void setMaxSpace(int max) {
    max_space.set(max);
  }
}
