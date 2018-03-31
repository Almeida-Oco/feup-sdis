package files;

import controller.Pair;
import controller.ApplicationInfo;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Future;
import java.io.FileNotFoundException;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.nio.channels.AsynchronousFileChannel;

/**
 * Handler of all file-system related input and output
 * @author Gonçalo Moreno
 * @author João Almeida
 * Singleton class
 */
class File_IO {
  public final static int MAX_CHUNK_SIZE = 64000;
  private final static String INIT_PATH  = "./stored_files";
  private static String PATH;

  /** Whether file-system was already setup or not */
  private static boolean setup = false;

  /**
   * Sets up the underlying file system for this peer
   * @param  peer_id ID of peer to use to create new directory
   * @return         Instance of {@link File_IO}, null if called twice
   * This function my only be called once throughout the whole program
   */
  static boolean setup(int peer_id) {
    if (!setup) {
      PATH = INIT_PATH + peer_id + "/";
      File dir = new File(PATH);

      try {
        return dir.mkdir();
      }
      catch (SecurityException err) {
        System.err.println("Failed to create '" + PATH + "' directory!\n - " + err.getMessage());
        return false;
      }
    }
    return false;
  }

  /**
   * Reads a given file from the system
   * @param  file_name  The path to the file to be read
   * @param  rep_degree Desired replication degree of the file
   * @return            Information about the file read
   */
  static FileInfo readFile(String file_name, int rep_degree) {
    File            fd = new File(file_name);
    int             chunk_n, bytes_read;
    FileInputStream reader;

    if ((chunk_n = numberOfChunks(fd)) == -1 || (reader = openFileReader(fd)) == null) {
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
        file.addChunk(new LocalChunk(buf, bytes_read, i, rep_degree));
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
    String f_name = file.getAbsolutePath();

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
   * Stores a single file in the filesystem
   * @param  file_name Name of the file to store
   * @param  data      Data to store in the file
   * @param  length    Length of the data to store in file
   * @return           Whether the store was successfull or not
   */
  static boolean storeFile(String file_name, byte[] data, int length) {
    File file = new File(PATH + file_name);

    try {
      FileOutputStream writer = new FileOutputStream(file);
      writer.write(data, 0, length);
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

  static boolean eraseFile(String file_name) {
    File file = new File(PATH + file_name);

    try {
      return file.delete();
    }
    catch (SecurityException err) {
      System.err.println("Denied access to file '" + file.getAbsolutePath() + "'\n - " + err.getMessage());
      return false;
    }
  }

  /**
   * Restores a file using the given chunks
   * @param  file_name Path to the file in the filesystem
   * @param  chunks    The chunks of the file
   * @return           Whether the file was successfully restored or not
   */
  static boolean restoreFile(String file_name, byte[][] chunks) {
    AsynchronousFileChannel out;

    if ((out = openFileWriter(file_name)) == null) {
      return false;
    }
    Vector<Future<Integer> > futures = new Vector<Future<Integer> >(chunks.length);
    for (int i = 0; i < chunks.length; i++) {
      byte[] chunk = chunks[i];
      futures.add(out.write(ByteBuffer.wrap(chunk), MAX_CHUNK_SIZE * i));
    }

    try {
      for (Future<Integer> future : futures) {
        future.get();
      }
      return true;
    }
    catch (InterruptedException | ExecutionException err) {
      System.err.println("Interrupted restore futures!\n - " + err.getMessage());
      return false;
    }
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
}
