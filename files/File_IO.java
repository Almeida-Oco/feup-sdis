package files;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class File_IO {
  public final static int MAX_CHUNK_SIZE = 30000;
  private final static int MAX_N_CHUNKS  = 999999;
  private final static String PATH       = "./stored_files/";

  //Contains the local files which were sent for backup
  private static ConcurrentHashMap<String, FileInfo> file_table = new ConcurrentHashMap<String, FileInfo>();

  public static void incReplication(String file_id, int chunk_n) {
    FileInfo file = file_table.get(file_id);

    if (file == null) {
      System.err.println("File '" + file_id + "' not in database!");
      return;
    }

    FileChunk chunk = file.getChunk(chunk_n);
    if (chunk == null) {
      return;
    }
    chunk.incActualRep();
  }

  public static void addFile(FileInfo file) {
    file_table.put(file.getName(), file);
  }

  public static FileInfo getFileInfo(String file_name) {
    if (file_name == null) {
      return null;
    }
    return file_table.get(file_name);
  }

  public static FileChunk getChunk(String file_id, int chunk_n) {
    FileInfo file = file_table.get(file_id);

    return file.getChunk(chunk_n);
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

  public static boolean storeChunk(String file_id, FileChunk chunk) {
    File chunk_file = new File(PATH + file_id + '#' + chunk.getChunkN());
    File directory  = new File(PATH);

    //TODO need to store file_chunk in table
    try {
      directory.mkdir();
      FileOutputStream writer = new FileOutputStream(chunk_file);
      writer.write(chunk.getData());
      FileInfo file = file_table.putIfAbsent(file_id, new FileInfo(file_id, chunk));
      if (file != null) {
        file.addChunk(chunk);
      }

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

  public static boolean fileExists(String file_name) {
    File f = new File(file_name);

    return f.exists() && !f.isDirectory();
  }

  public static boolean eraseChunksOf(String file_name) {
    FileInfo file   = file_table.get(file_name);
    boolean  erased = true;

    if (file != null) {
      Vector<FileChunk> chunks = file.getChunks();
      int size = chunks.size();
      for (int i = 0; i < size; i++) {
        FileChunk chunk = chunks.get(i);
        erased = erased && eraseChunk(file_name + chunk.getChunkN());
        if (erased) {
          file.eraseChunk(chunk);
        }
      }
      file_table.remove(file_name);
      return erased;
    }

    return true;
  }

  public static boolean eraseFile(String file_name) {
    File file = new File(file_name);

    return file.delete();
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

  public static ConcurrentHashMap<String, FileInfo> getTable() {
    return file_table;
  }
}
