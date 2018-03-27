package files;

import controller.Pair;

import java.io.File;
import java.util.Set;
import java.util.Map;
import java.util.Vector;
import java.io.IOException;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

public class File_IO {
  public final static int MAX_CHUNK_SIZE = 64000;
  private final static int MAX_N_CHUNKS  = 999999;
  private final static String PATH       = "./stored_files/";

  //Contains the local files which were sent for backup
  private static ConcurrentHashMap<String, FileInfo> file_table = new ConcurrentHashMap<String, FileInfo>();

  //Contains the chunks stored by the peer
  private static ConcurrentHashMap<String, Vector<FileChunk> > stored_chunks = new ConcurrentHashMap<String, Vector<FileChunk> >();


  public static void tryIncRep(String file_id, int chunk_n, int peer_id) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    if (chunks == null) {
      return;
    }
    int index = FileChunk.binarySearch(chunks, chunk_n);
    if (index == -1) {
      return;
    }

    chunks.get(index).addPeer(peer_id);
  }

  public static void addFile(FileInfo file) {
    file_table.put(file.getName(), file);
  }

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

  public static boolean storeChunk(String file_id, FileChunk chunk) {
    stored_chunks.putIfAbsent(file_id, new Vector<FileChunk>());
    File chunk_file = new File(PATH + file_id + '#' + chunk.getChunkN());
    File directory  = new File(PATH);

    try {
      directory.mkdir();
      FileOutputStream writer = new FileOutputStream(chunk_file);
      writer.write(chunk.getData(), 0, chunk.getSize());

      addChunk(file_id, chunk);
      writer.close();
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

  public static void eraseLocalFile(String file_id) {
    File file = new File(file_id);

    try {
      file.delete();
    }
    catch (SecurityException err) {
      System.err.println("Security manager denied access to file '" + file_id + "'!\n - " + err.getMessage());
    }
  }

  public static void eraseChunk(String file_id, int chunk_n, boolean rm_from_table) {
    String path = PATH + file_id + "#" + chunk_n;

    try {
      File chunk = new File(path);
      chunk.delete();
      Vector<FileChunk> chunks = stored_chunks.get(file_id);
      if (rm_from_table) {
        chunks.remove(FileChunk.binarySearch(chunks, chunk_n));
      }
    }
    catch (SecurityException err) {
      System.err.println("Security manager denied access to file '" + path + "'!\n - " + err.getMessage());
    }
  }

  public static boolean eraseFileChunks(String file_id) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);

    if (chunks == null) {
      return false;
    }

    chunks.forEach((chunk)->eraseChunk(file_id, chunk.getChunkN(), false));
    stored_chunks.remove(file_id);
    return true;
  }

  public static boolean restoreFile(String file_name, Vector<FileChunk> chunks) {
    chunks.sort(null);
    FileOutputStream out;

    if ((out = openFileWriter(file_name)) == null) {
      return false;
    }

    try {
      for (FileChunk chunk : chunks) {
        out.write(chunk.getData());
      }
      out.close();
    }
    catch (IOException err) {
      System.err.println("Failed to write to FD of '" + file_name + "'\n - " + err.getMessage());
      return false;
    }
    return true;
  }

  private static FileOutputStream openFileWriter(String file_name) {
    try {
      return new FileOutputStream(file_name);
    }
    catch (FileNotFoundException err) {
      System.err.println("Failed to create file '" + file_name + "'\n - " + err.getMessage());
      return null;
    }
    catch (SecurityException err) {
      System.err.println("Access denied to file '" + file_name + "'\n - " + err.getMessage());
      return null;
    }
  }

  public static FileInfo getFileInfo(String file_name) {
    if (file_name == null) {
      return null;
    }
    return file_table.get(file_name);
  }

  public static FileChunk getStoredChunk(String file_id, int chunk_n) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    if (chunks == null) {
      return null;
    }

    int index = FileChunk.binarySearch(chunks, chunk_n);
    System.out.println("Index = " + index);
    if (index == -1) {
      System.err.println("Chunk #" + chunk_n + " is not stored locally!");
      return null;
    }

    return chunks.get(index);
  }

  public static int chunkRepDegree(String file_id, int chunk_n) {
    FileChunk chunk = getStoredChunk(file_id, chunk_n);

    if (chunk != null) {
      return chunk.getActualRep();
    }
    return -1;
  }

  private static void addChunk(String file_id, FileChunk chunk) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    int index = Collections.binarySearch(chunks, chunk);

    if (index < 0) {
      chunks.add(-(index) - 1, chunk);
    }
    else {
      System.out.println("Replicated chunk #" + chunk.getChunkN());
    }
  }

  public static ConcurrentHashMap<String, FileInfo> getBackedUpTable() {
    return file_table;
  }

  public static ConcurrentHashMap<String, Vector<FileChunk> > getChunksTable() {
    return stored_chunks;
  }

  public static Vector<Pair<String, FileChunk> > reclaimSpace(long bytes) {
    Vector<Pair<String, FileChunk> > rem_chunks = new Vector<Pair<String, FileChunk> >();
    long rem_bytes = bytes;
    int  size = stored_chunks.size(), has_chunks = size;

    for (int i = 0; i < MAX_N_CHUNKS && has_chunks > 0 && rem_bytes > 0; i++, has_chunks = size) {
      for (Map.Entry<String, Vector<FileChunk> > entry : stored_chunks.entrySet()) {
        if (i < entry.getValue().size()) {
          FileChunk chunk = entry.getValue().get(i);
          rem_chunks.add(new Pair<String, FileChunk>(entry.getKey(), chunk));
          rem_bytes -= chunk.getSize();
        }
        else {
          has_chunks--;
        }
      }
    }

    System.out.println("Reclaiming " + (bytes - rem_bytes) + " bytes!");
    return rem_chunks;
  }

  private static void removeChunks(Vector<Pair<String, FileChunk> > chunks) {
    chunks.forEach((pair)->{
      Vector<FileChunk> file_chunks = stored_chunks.get(pair.first());
      if (file_chunks != null) {
        int index = Collections.binarySearch(file_chunks, pair.second());
        if (index >= 0) {
          file_chunks.remove(index);
        }
      }
    });
  }
}
