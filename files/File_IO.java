package files;

import controller.Pair;
import controller.ApplicationInfo;

import java.io.File;
import java.util.Set;
import java.util.Map;
import java.util.Vector;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Future;
import java.io.FileNotFoundException;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handler of all file-system related input and output
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class File_IO {
  public final static int MAX_CHUNK_SIZE = 64000;
  private final static int MAX_N_CHUNKS  = 999999;
  private final static String PATH       = "./stored_files";

  /** Maximum space allocated to the program in bytes */
  private static AtomicInteger max_space = new AtomicInteger(8192000);  //Equivalent to 8MB

  /** Space used by the program in bytes */
  private static AtomicInteger used_space = new AtomicInteger(0);       //Equivalent to 8MB

  /**
   * Holds the local files which were sent for backup
   * Key is real path to file
   */
  private static ConcurrentHashMap<String, FileInfo> file_table = new ConcurrentHashMap<String, FileInfo>();

  /**
   * Holds the chunks stored by the peer
   * Key is the ID of the file
   */
  private static ConcurrentHashMap<String, Vector<FileChunk> > stored_chunks = new ConcurrentHashMap<String, Vector<FileChunk> >();

  /**
   * Tries to increment the actual replication degree of a given chunk
   * @param file_id ID of file
   * @param chunk_n Number of the chunk
   * @param peer_id The peer that stored the chunk
   */
  public static void tryIncRep(String file_id, int chunk_n, int peer_id) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    if (chunks == null) {
      return;
    }

    synchronized (stored_chunks) {
      int index = FileChunk.binarySearch(chunks, chunk_n);
      if (index == -1) {
        return;
      }

      chunks.get(index).addPeer(peer_id);
    }
  }

  public static boolean isLocalFile(String file_id) {
    for (Map.Entry<String, FileInfo> entry : file_table.entrySet()) {
      System.out.println("---\nID in table '" + entry.getValue().getID() + "'");
      System.out.println("ID received '" + file_id + "'\n---");
      if (entry.getValue().getID().equals(file_id)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds a new file to the {@link File_IO#file_table}
   * @param file File to be added to the table
   */
  public static void addFile(FileInfo file) {
    file_table.put(file.getName(), file);
  }

  /**
   * Reads a given file from the system
   * @param  file_name  The path to the file to be read
   * @param  rep_degree Desired replication degree of the file
   * @return            Information about the file read
   */
  public static FileInfo readFile(String file_name, int rep_degree) {
    File            fd = new File(file_name);
    int             chunk_n, bytes_read;
    FileInputStream reader;

    if ((chunk_n = File_IO.numberOfChunks(fd)) == -1 || (reader = File_IO.openFileReader(fd)) == null) {
      return null;
    }

    FileInfo file = new FileInfo(fd, chunk_n, rep_degree);
    for (int i = 0; i < chunk_n; i++) {
      byte[] buf = new byte[MAX_CHUNK_SIZE];
      bytes_read = File_IO.readFromFile(reader, buf);

      if (bytes_read == -1) {
        return null;
      }
      else {
        file.addChunk(new FileChunk(buf, bytes_read, i, rep_degree));
      }
    }
    return file;
  }

  /**
   * Computes the number of chunks the given file will be split into
   * @param  file File to be used
   * @return      Number of chunks
   */
  private static int numberOfChunks(File file) {
    if (!file.exists()) {
      System.err.println("File '" + file.getName() + "' does not exist!");
      return -1;
    }

    if (file.isDirectory()) {
      System.err.println("File '" + file.getName() + "' is a directory!");
      return -1;
    }

    long file_size;
    if ((file_size = file.length()) == 0L) {
      System.err.println("Cannot get length from a system-dependent entity!");
      return -1;
    }

    if (file_size % File_IO.MAX_CHUNK_SIZE == 0) {
      return (int)(Math.ceil(file_size * 1.0 / File_IO.MAX_CHUNK_SIZE) + 1);
    }
    else {
      return (int)(Math.ceil(file_size * 1.0 / File_IO.MAX_CHUNK_SIZE));
    }
  }

  /**
   * Reads from the file until buf is filled
   * @param  stream Stream of bytes from file
   * @param  buf    Buffer to store bytes read
   * @return        Number of bytes read, 0 if EOF reached
   */
  private static int readFromFile(FileInputStream stream, byte[] buf) {
    try {
      int bytes_read = stream.read(buf);
      return bytes_read == -1 ? 0 : bytes_read;
    }
    catch (IOException err) {
      System.err.println("Failed to read file from stream!\n - " + err.getMessage());
      return -1;
    }
  }

  /**
   * Opens a file to be read
   * @param  file {@link File} information
   * @return      A new stream of bytes of the file
   */
  private static FileInputStream openFileReader(File file) {
    String f_name = file.getName();

    try {
      return new FileInputStream(file);
    }
    catch (FileNotFoundException err) {
      System.err.println("Failed to open file '" + f_name + "'\n - " + err.getMessage());
      return null;
    }
    catch (SecurityException err) {
      System.err.println("Access denied to file '" + f_name + "'\n - " + err.getMessage());
      return null;
    }
  }

  /**
   * Stores the chunk locally
   * @param  file_id ID of the file
   * @param  chunk   Number of the chunk
   * @return         Whether the file was successfully stored or not
   */
  public static boolean storeChunk(String file_id, FileChunk chunk) {
    stored_chunks.putIfAbsent(file_id, new Vector<FileChunk>());
    File chunk_file = new File(PATH + ApplicationInfo.getServID() + "/" + file_id + '#' + chunk.getChunkN());
    File directory  = new File(PATH + ApplicationInfo.getServID() + "/");

    try {
      directory.mkdir();
      if (!addChunk(file_id, chunk)) {
        return false;
      }

      FileOutputStream writer = new FileOutputStream(chunk_file);
      writer.write(chunk.getData(), 0, chunk.getSize());
      writer.close();
      chunk.addPeer(ApplicationInfo.getServID());
      return true;
    }
    catch (FileNotFoundException err) {
      System.err.println("Failed to open descriptor to file\n - " + err.getCause() + ": " + err.getMessage());
      return false;
    }
    catch (IOException err) {
      System.err.println("Failed to write data to file\n - " + err.getMessage());
      return false;
    }
  }

  /**
   * Erases a local file from the system
   * @param file_id ID of file to be deleted
   */
  public static void eraseLocalFile(String file_id) {
    File file = new File(file_id);

    try {
      file.delete();
      file_table.remove(file_id);
    }
    catch (SecurityException err) {
      System.err.println("Security manager denied access to file '" + file_id + "'!\n - " + err.getMessage());
    }
  }

  /**
   * Erases a chunk from the system
   * @param file_id       ID of file to erase chunk of
   * @param chunk_n       Number of chunk to erase
   * @param rm_from_table Whether to remove the chunk from {@link File_IO#stored_chunks} or not
   */
  public static void eraseChunk(String file_id, int chunk_n, boolean rm_from_table) {
    String path = PATH + ApplicationInfo.getServID() + "/" + file_id + "#" + chunk_n;

    try {
      File chunk = new File(path);
      chunk.delete();
      Vector<FileChunk> chunks = stored_chunks.get(file_id);
      int index = FileChunk.binarySearch(chunks, chunk_n);
      used_space.addAndGet(-chunks.get(index).getSize());

      if (rm_from_table) {
        chunks.remove(index);
      }
    }
    catch (SecurityException err) {
      System.err.println("Security manager denied access to file '" + path + "'!\n - " + err.getMessage());
    }
  }

  /**
   * Erases all chunks of a file
   * @param  file_id ID of file to delete the chunks
   * @return         Whether all chunks were successfully erased or not
   */
  public static boolean eraseFileChunks(String file_id) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);

    if (chunks == null) {
      return false;
    }

    chunks.forEach((chunk)->eraseChunk(file_id, chunk.getChunkN(), false));
    stored_chunks.remove(file_id);
    return true;
  }

  /**
   * Restores a file using the given chunks
   * @param  file_name Path to the file in the filesystem
   * @param  chunks    The chunks of the file
   * @return           Whether the file was successfully restored or not
   */
  public static boolean restoreFile(String file_name, Vector<FileChunk> chunks) {
    chunks.sort(null);
    AsynchronousFileChannel out;

    if ((out = openFileWriter(file_name)) == null) {
      return false;
    }
    Vector<Future<Integer> > futures = new Vector<Future<Integer> >(chunks.size());
    try {
      for (FileChunk chunk : chunks) {
        futures.add(out.write(ByteBuffer.wrap(chunk.getData()), MAX_CHUNK_SIZE * chunk.getChunkN()));
      }
      for (Future<Integer> future : futures) { //Wait for write to finish
        future.get();
      }
    }
    catch (InterruptedException err) {
      System.err.println("Interrupted restore futures!\n - " + err.getMessage());
      return false;
    }
    catch (ExecutionException err) {
      System.err.println("Restore future threw an exception!\n - " + err.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Opens a stream to write to a file
   * @param  file_name Path to file to write to
   * @return           The newly created stream or null on error
   * It always creates a new file, if file already exists an exception is thrown
   */
  private static AsynchronousFileChannel openFileWriter(String file_name) {
    try {
      return AsynchronousFileChannel.open(Paths.get(file_name), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
    }
    catch (IllegalArgumentException err) {
      System.err.println("Wrong options!\n - " + err.getMessage());
    }
    catch (UnsupportedOperationException err) {
      System.err.println("Creation of file channels not supported!\n - " + err.getMessage());
    }
    catch (SecurityException err) {
      System.err.println("Access denied to file '" + file_name + "'\n - " + err.getMessage());
    }
    catch (IOException err) {
      System.err.println("I/O error occurred!\n - " + err.getMessage());
    }

    return null;
  }

  /**
   * Gets information about a file from the table
   * @param  file_name Path to the file
   * @return           The information of the file, null if non-existing
   */
  public static FileInfo getFileInfo(String file_name) {
    if (file_name == null) {
      return null;
    }
    return file_table.get(file_name);
  }

  /**
   * Gets a stored chunk from the {@link File_IO#stored_chunks}
   * @param  file_id ID of file
   * @param  chunk_n Number of chunk
   * @return         {@link FileChunk} or null if non-existing
   */
  public static FileChunk getStoredChunk(String file_id, int chunk_n) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    if (chunks == null) {
      return null;
    }

    int index = FileChunk.binarySearch(chunks, chunk_n);
    System.out.println("Index = " + index);
    if (index == -1) {
      return null;
    }

    return chunks.get(index);
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
    return max_space.get() - used_space.get();
  }

  /**
   * Gets the actual replication degree of a chunk
   * @param  file_id ID of file
   * @param  chunk_n Number of the chunk
   * @return         Replication degree of chunk, -1 if not found
   */
  public static int chunkRepDegree(String file_id, int chunk_n) {
    FileChunk chunk = getStoredChunk(file_id, chunk_n);

    if (chunk != null) {
      return chunk.getActualRep();
    }
    return -1;
  }

  /**
   * Adds a chunk to {@link File_IO#stored_chunks}
   * @param  file_id ID of the file
   * @param  chunk   Number of the chunk
   * @return         Whether the chunk was added or not (a chunk is not added if it is replicated)
   */
  private static boolean addChunk(String file_id, FileChunk chunk) {
    synchronized (stored_chunks) {
      Vector<FileChunk> chunks = stored_chunks.get(file_id);
      int index = Collections.binarySearch(chunks, chunk);

      if (index < 0) {
        used_space.addAndGet(chunk.getSize());
        chunks.add(-(index) - 1, chunk);
        return true;
      }
    }

    return false;
  }

  /**
   * Gets the table of files backed up by the network
   * @return {@link File_IO#file_table}
   */
  public static ConcurrentHashMap<String, FileInfo> getBackedUpTable() {
    return file_table;
  }

  /**
   * Gets the table of chunks stored by the peer
   * @return {@link File_IO#stored_chunks}
   */
  public static ConcurrentHashMap<String, Vector<FileChunk> > getChunksTable() {
    return stored_chunks;
  }

  /**
   * Reclaims the given amount of bytes from the protocol
   * @param  bytes Number of bytes to reclaim
   * @return       Vector with chunks to remove in order to reclaim the given amount of space
   * The algorith first retrieves all overly replicated chunks, if the reclaimed space is not enough it starts by removing one chunk of each stored file
   */
  public static Vector<Pair<String, FileChunk> > reclaimSpace(long bytes) {
    Vector<Pair<String, FileChunk> > rem_chunks = new Vector<Pair<String, FileChunk> >();
    long rem_bytes = reclaimOverlyRep(rem_chunks, bytes);
    int  size = stored_chunks.size(), has_chunks = size;

    if (rem_bytes > 0) {
      for (int i = 0; i < MAX_N_CHUNKS && has_chunks > 0 && rem_bytes > 0; i++, has_chunks = size) {
        for (Map.Entry<String, Vector<FileChunk> > entry : stored_chunks.entrySet()) {
          if (i < entry.getValue().size()) {
            FileChunk chunk = entry.getValue().get(i);
            if (chunk.getActualRep() <= chunk.getDesiredRep()) {
              rem_chunks.add(new Pair<String, FileChunk>(entry.getKey(), chunk));
              rem_bytes -= chunk.getSize();
            }
          }
          else {
            has_chunks--;
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
  private static long reclaimOverlyRep(Vector<Pair<String, FileChunk> > rem_chunks, long bytes) {
    long rem_bytes = bytes;
    int  size = stored_chunks.size(), has_chunks = size;

    for (int i = 0; i < MAX_N_CHUNKS && has_chunks > 0 && rem_bytes > 0; i++, has_chunks = size) {
      for (Map.Entry<String, Vector<FileChunk> > entry : stored_chunks.entrySet()) {
        if (i < entry.getValue().size()) {
          FileChunk chunk = entry.getValue().get(i);
          if (chunk.getActualRep() > chunk.getDesiredRep()) {
            rem_chunks.add(new Pair<String, FileChunk>(entry.getKey(), chunk));
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

  /**
   * Sets the maximum amount of bytes available to the service
   * @param max Number of bytes available
   */
  public static void setMaxSpace(int max) {
    max_space.set(max);
  }
}
