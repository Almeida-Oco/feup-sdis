package files;

import java.util.HashMap;
import java.util.Vector;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

public class File_IO {
  private final static int MAX_CHUNK_SIZE = 64000;
  private final static int MAX_N_CHUNKS   = 999999;


  //value of hashmap will be a vector with a FileInfo for every replication
  // Not sure if this is needed though, for now it is not being used
  private static HashMap<String, FileInfo> file_table = new HashMap<String, FileInfo>();

  public static FileChunk readChunk(String file_name) {
    try {
      FileInputStream reader = new FileInputStream(file_name);
      byte[]          buf    = new byte[MAX_CHUNK_SIZE];

      reader.read(buf);
      return new FileChunk(file_name, buf);
    }
    catch (IOException err) {
      System.err.println("Failed to read chunk from stream!\n " + err.getMessage());
      return null;
    }
  }

  public static Vector<FileChunk> readFile(String file_name) {
    File            file = new File(file_name);
    int             chunk_n;
    FileInputStream reader;

    if ((chunk_n = File_IO.numberOfChunks(file)) == -1 || (reader = File_IO.openFile(file)) == null) {
      return null;
    }

    Vector<FileChunk> chunks = new Vector<FileChunk>(chunk_n);
    for (int i = 0; i < chunk_n; i++) {
      byte[] buf = new byte[MAX_CHUNK_SIZE];
      int    ret = File_IO.readFromFile(reader, buf);

      if (ret == -1) {
        return null;
      }
      else { //ret == 0 means its the last chunk with size 0
        chunks.add(new FileChunk((ret == 0) ? new byte[0] : buf));
      }
    }
    return chunks;
  }

  public static boolean storeFile(String file_name, byte[] data, byte version) {
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
      return bytes_read == -1 ? 0 : bytes_read + 1;
    }
    catch (IOException err) {
      System.err.println("Failed to read file from stream!\n " + err.getMessage());
      return -1;
    }
  }
}
