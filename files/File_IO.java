package files;

import java.util.concurrent.ConcurrentHashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class File_IO {
  private final static int MAX_CHUNK_SIZE = 64000;
  private final static int MAX_N_CHUNKS   = 999999;


  //Contains the local files which were sent for backup
  private static ConcurrentHashMap<String, FileInfo> file_table = new ConcurrentHashMap<String, FileInfo>();

  public static void addFile(FileInfo file) {
    file_table.put(file.getName(), file);
  }

  public static FileInfo getFileInfo(String file_name) {
    if (file_name == null) {
      return null;
    }
    return file_table.get(file_name);
  }

  public static FileChunk readChunk(String file_name) {
    try {
      FileInputStream reader     = new FileInputStream(file_name);
      byte[]          buf        = new byte[MAX_CHUNK_SIZE];
      int             bytes_read = reader.read(buf);

      return new FileChunk(buf, (bytes_read == -1) ? 0 : bytes_read);
    }
    catch (IOException err) {
      System.err.println("Failed to read chunk from stream!\n " + err.getMessage());
      return null;
    }
  }

  public static FileInfo readFile(String file_name) {
    File            fd = new File(file_name);
    int             chunk_n, bytes_read;
    FileInputStream reader;

    if ((chunk_n = File_IO.numberOfChunks(fd)) == -1 || (reader = File_IO.openFile(fd)) == null) {
      return null;
    }

    FileInfo file = new FileInfo(fd, chunk_n);
    for (int i = 0; i < chunk_n; i++) {
      byte[] buf = new byte[MAX_CHUNK_SIZE];
      bytes_read = File_IO.readFromFile(reader, buf);

      if (bytes_read == -1) {
        return null;
      }
      else { //bytes_read == 0 means its the last chunk with size 0
        file.addChunk(new FileChunk((bytes_read == 0) ? new byte[0] : buf, bytes_read));
      }
    }
    return file;
  }

  public static boolean storeFile(String file_name, byte[] data) {
    try {
      FileOutputStream writer = new FileOutputStream(file_name);
      writer.write(data);

      return true;
    }
    catch (FileNotFoundException err) {
      System.err.println("Failed to open descriptor to file '" + file_name + "'\n " + err.getCause() + ": " + err.getMessage());
      return false;
    }
    catch (IOException err) {
      System.err.println("Failed to write data to file '" + file_name + "'\n " + err.getMessage());
      return false;
    }
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

  private static FileInputStream openFile(File file) {
    FileInputStream stream;
    String          f_name = file.getName();

    try {
      stream = new FileInputStream(file);
      return stream;
    }
    catch (FileNotFoundException err) {
      System.err.println("Failed to open file '" + f_name + "'\n " + err.getMessage());
      return null;
    }
    catch (SecurityException err) {
      System.err.println("Access denied to file '" + f_name + "'\n " + err.getMessage());
      return null;
    }
  }

  private static int readFromFile(FileInputStream stream, byte[] buf) {
    try {
      int bytes_read = stream.read(buf);
      return bytes_read == -1 ? 0 : bytes_read;
    }
    catch (IOException err) {
      System.err.println("Failed to read file from stream!\n " + err.getMessage());
      return -1;
    }
  }
}
