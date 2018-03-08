package files;

import java.util.Vector;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileInfo {
  private final static int MAX_CHUNK_SIZE = 64000;
  String file_name;
  String file_id;
  String metadata;
  Vector<FileChunk> chunks;

  FileInfo(File fd, int chunk_number) {
    String abs_path   = fd.getAbsolutePath(),
           last_mod   = Long.toString(fd.lastModified());
    int metadata_size = abs_path.length() + last_mod.length() + 2 * MAX_CHUNK_SIZE;

    //TODO calculate metadata_size differently
    this.metadata  = new String(new byte[metadata_size]);
    this.metadata += abs_path + last_mod;
    this.file_name = abs_path;
    this.chunks    = new Vector<FileChunk>(chunk_number);
  }

  void addChunk(FileChunk chunk) {
    if (this.chunks.size() == 0) { //Use first chunk as hash
      this.metadata += new String(chunk.getData());
    }

    this.chunks.add(chunk);

    if (chunk.isFinalChunk()) { //Use last chunk as hash
      this.metadata += new String(chunk.getData());
      this.setFileID();
    }
  }

  private void setFileID() {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-256");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println("Failed to find encryption algorithm!\n - " + err.getMessage() + "\n - How the frick is this possible?");
      intestine = null;
      System.exit(0);
    }
    this.file_id = new String(intestine.digest(this.metadata.getBytes()));
  }

  public String getName() {
    return this.file_name;
  }

  public Vector<FileChunk> getChunks() {
    return this.chunks;
  }
}
