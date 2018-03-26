package files;

import controller.Pair;

import java.io.File;
import java.util.Set;
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

    FileInfo file = new FileInfo(fd, chunk_n);
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

  public static boolean eraseFileChunks(String file_id) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);

    if (chunks == null) {
      return false;
    }

    chunks.forEach((chunk)->{
      String chunk_name = PATH + file_id + "#" + chunk.getChunkN();
      File chunk_file   = new File(chunk_name);
      try {
        chunk_file.delete();
      }
      catch (SecurityException err) {
        System.err.println("Security manager denied access to file '" + chunk_name + "'!\n - " + err.getMessage());
      }
    });
    stored_chunks.remove(file_id);
    return true;
  }

  public static boolean eraseChunk(String chunk_id) {
    File chunk = new File(PATH + chunk_id);

    try {
      return chunk.delete();
    }
    catch (SecurityException err) {
      System.err.println("Failed to delete chunk '" + chunk_id + "'\n - " + err.getMessage());
      return false;
    }
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

  public static FileChunk getChunk(String file_id, int chunk_n) {
    Vector<FileChunk> chunks = stored_chunks.get(file_id);
    if (chunks == null) {
      System.err.println("File '" + file_id + "' is not stored locally!");
      return null;
    }

    int index = FileChunk.binarySearch(chunks, chunk_n);
    if (index == -1) {
      System.err.println("Chunk #" + chunk_n + " is not stored locally!");
      return null;
    }

    return chunks.get(index);
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

  public static ConcurrentHashMap<String, FileInfo> getTable() {
    return file_table;
  }

  // public static Vector<Pair<String, FileChunk> > reclaimSpace(long space) {
  //   Vector<Pair<String, FileChunk> > overly_rep = getOverlyReplicated();
  //   long rem_space = space, i, size = overly_rep.size();
  //   Vector<Pair<String, FileChunk> > chunks = new Vector<Pair<String, FileChunk> >(size);
  //
  //   for (i = 0; i < size && rem_space > 0; i++) {
  //     FileChunk chunk = overly_rep.at(i);
  //     chunks.add(chunk);
  //     rem_space -= chunk.getSize();
  //   }
  //
  //   if (rem_space > 0) { //Still space left to reclaim
  //   }
  //
  //   return chunks;
  // }
  //
  // private static Vector<Pair<String, FileChunk> > reclaimRemaining(long space) {
  //   Vector<Pair<String, FileChunk> > chunks = new Vector<Pair<String, FileChunk> >();
  // }
  //
  // private static Vector<Pair<String, FileChunk> > getOverlyReplicated() {
  //   Vector<Pair<String, FileChunk> > chunks = new Vector<Pair<String, FileChunk> >();
  //   file_table.forEach((file_name, file_info)->{
  //     chunks.addAll(file_info.getOverlyReplicated());
  //   });
  // }
}
