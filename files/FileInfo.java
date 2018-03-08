package files;

import java.util.Vector;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileInfo {
  String file_name;
  String file_id;
  String metadata;
  Vector<FileChunk> chunks;

  FileInfo(File fd, int chunk_number) {
    long metadata_size = fd.getAbsolutePath().length() + Long.toString(fd.lastModified()).length() + fd.length();

    //TODO calculate metadata_size differently
    this.metadata  = new String(new byte[metadata_size]);
    this.metadata += fd.getAbsolutePath() + Long.toString(fd.lastModified());
    this.file_name = fd.getAbsolutePath();
    this.chunks    = new Vector<FileChunk>(chunk_number);
  }

  void addChunk(FileChunk chunk) {
    this.chunks.add(chunk);

    if (chunk.isFinalChunk()) {
      this.setFileID();
    }
  }

  private void setFileID() {
    MessageDigest intestine;

    try {
      intestine = MessageDigest.getInstance("SHA-256");
    }
    catch (NoSuchAlgorithmException err) {
      System.err.println("Failed to find encryption algorithm!\n " + err.getMessage() + "\n  How the frick is this possible?");
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
