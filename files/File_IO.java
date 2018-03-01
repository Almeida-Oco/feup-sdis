package files;

import java.util.HashMap;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

public class File_IO {
  private final static int MAX_CHUNK_SIZE = 64000;
  private final static int MAX_N_CHUNKS   = 999999;

  //TODO value of hashmap will be a vector with a FileInfo for every replication
  private static HashMap <String, FileInfo> file_table = new HashMap <String, FileInfo>();


  //TODO should try every FileInfo of the given <file_name> until it is able to read from one
  public static byte[] readFile(String file_name) {
    FileInfo info = File_IO.file_table.get(file_name);

    if (info == null) {
      return null;
    }
    try {
      FileInputStream reader = new FileInputStream(info.getName());
      byte[]          buf    = new byte[MAX_CHUNK_SIZE];
      reader.read(buf);

      return buf;
    }
    catch (FileNotFoundException err) {
      System.err.println("Could not open file '" + info.getName() + "' for reading!\n " + err.getMessage());
      return null;
    }
    catch (IOException err) {
      System.err.println("Could not read from file '" + info.getName() + "'\n " + err.getMessage());
      return null;
    }
  }

  // TODO place where file is stored is different from file_name
  // file_name is the name of the file, whereas the file might be stored in '/home/user/sdis/replication1/<file_name>'
  // what will be store in the FileInfo is the actual path to the file, and the key of the file_table is the <file_name>
  public static boolean storeFile(String file_name, byte[] data, byte version) {
    try {
      FileOutputStream writer = new FileOutputStream(file_name);
      writer.write(data);
      File_IO.file_table.put(file_name, new FileInfo(file_name, version));

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
}
