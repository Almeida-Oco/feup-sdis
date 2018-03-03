package files;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileChunk {
  String chunk_id;
  byte[] chunk_data;


  FileChunk(byte[] data) {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-256");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println("Failed to find encryption algorithm!\n " + err.getMessage() + "\n  How the frick is this possible?");
      intestine = null;
      System.exit(0);
    }

    this.chunk_id   = new String(intestine.digest(data));
    this.chunk_data = data;
  }

  FileChunk(String id, byte[] data) {
    this.chunk_id   = id;
    this.chunk_data = data;
  }

  public String get_id() {
    return this.chunk_id;
  }

  public byte[] get_data() {
    return this.chunk_data;
  }
}
