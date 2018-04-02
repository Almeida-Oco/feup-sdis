package files;

import controller.Pair;

import java.io.File;
import java.util.Vector;
import java.util.Collections;
import java.security.MessageDigest;
import java.security.DigestException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Holds information about a backed up file
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class FileInfo {
  private final static int HASH_SIZE = 32;
  private String file_name, metadata; //Used only for hashing purposes

  /** ID of file */
  String file_id;

  /** Chunks of the file */
  Vector<Chunk> chunks;

  /** Desired replication degree of file */
  int desired_rep;

  /**
   * Initializes a new {@link FileInfo}
   * @param fd           Descriptor to file
   * @param chunk_number Number of chunks
   * @param rep_degree   Desired replication degree
   */
  FileInfo(File fd, int chunk_number, int rep_degree) {
    String abs_path   = fd.getAbsolutePath(),
        last_mod      = Long.toString(fd.lastModified());
    int metadata_size = abs_path.length() + last_mod.length() + 2 * File_IO.MAX_CHUNK_SIZE;

    this.metadata    = new String(new byte[metadata_size]);
    this.metadata   += abs_path + last_mod;
    this.file_name   = abs_path;
    this.chunks      = new Vector<Chunk>(chunk_number);
    this.file_id     = null;
    this.desired_rep = rep_degree;
  }

  /**
   * Adds a new chunk
   * @param chunk {@link Chunk} to be added to {@link FileInfo#chunks}
   */
  void addChunk(Chunk chunk) {
    int index = Collections.binarySearch(this.chunks, chunk.getChunkN());

    if (this.file_id == null) { // Still reading from file
      this.tryHash(chunk);
    }
    if (index >= 0) {
      System.out.println("Replicated chunk #" + chunk.getChunkN());
    }
    else {
      this.chunks.add(-(index) - 1, chunk);
    }
  }

  boolean addPeer(int chunk_n, Integer peer_id) {
    int index = Collections.binarySearch(this.chunks, chunk_n);

    if (index >= 0) {
      this.chunks.get(index).addPeer(peer_id);
    }
    return index >= 0;
  }

  boolean removePeer(int chunk_n, Integer peer_id) {
    int index = Collections.binarySearch(this.chunks, chunk_n);

    System.out.println("Info index = " + index);
    if (index >= 0) {
      this.chunks.get(index).removePeer(peer_id);
    }

    return index >= 0;
  }

  /**
   * Tries to hash the metadata
   * @param chunk Latest chunk to be added into {@link FileInfo#chunks}
   */
  private void tryHash(Chunk chunk) {
    if (this.chunks.size() == 0) { //Use first chunk as hash
      this.metadata += new String(chunk.getData());
    }

    if (chunk.isFinalChunk()) { //Use last chunk as hash
      this.metadata += new String(chunk.getData());
      this.setFileID();
    }
  }

  /**
   * Sets the ID of the file
   */
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
    intestine.update(this.metadata.getBytes());
    byte[] hash = new byte[32];

    try {
      int    size   = intestine.digest(hash, 0, HASH_SIZE);
      String middle = new String(hash, 0, HASH_SIZE, StandardCharsets.ISO_8859_1);
      this.file_id = StringToHex.toHex(middle.getBytes(StandardCharsets.ISO_8859_1), HASH_SIZE);
    }
    catch (DigestException err) {
      System.err.println("Failed to digest!\n - " + err.getMessage());
    }
  }

  /**
   * Gets the name of the file
   * @return {@link FileInfo#file_name}
   */
  public String getName() {
    return this.file_name;
  }

  /**
   * Gets the ID of the file
   * @return {@link FileInfo#file_id}
   */
  public String getID() {
    return this.file_id;
  }

  /**
   * Gets a specific chunk from {@link FileInfo#chunks}
   * @param  chunk_n Number of chunk to fetch
   * @return         The requested chunk, null if not found
   */
  public Chunk getChunk(int chunk_n) {
    int index = Collections.binarySearch(this.chunks, chunk_n);

    if (index < 0) {
      System.err.println("Chunk #" + chunk_n + " not stored in file " + this.file_name);
      return null;
    }

    return this.chunks.get(index);
  }

  /**
   * Gets the file desired replication degree
   * @return {@link FileInfo#desired_rep}
   */
  public int getDesiredRep() {
    return this.desired_rep;
  }

  /**
   * Gets the number of chunks of the file
   * @return Number of chunks of file
   */
  public int chunkNumber() {
    return this.chunks.size();
  }

  public Vector<Chunk> underlyReplicated() {
    Vector<Chunk> underly_chunks = new Vector<Chunk>();
    for (Chunk chunk : this.chunks) {
      if (chunk.getActualRep() < chunk.getDesiredRep()) {
        underly_chunks.add(chunk);
      }
    }

    return underly_chunks;
  }

  /**
   * Gets all the chunks of the file
   * @return {@link FileInfo#chunks}
   */
  public Vector<Chunk> getChunks() {
    return this.chunks;
  }
}
