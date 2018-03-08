package files;

public class FileChunk {
  private final static int MAX_CHUNK_SIZE = 64000;
  byte[] chunk_data;
  int chunk_size;


  FileChunk(byte[] data, int size) {
    this.chunk_data = data;
    this.chunk_size = size;
  }

  public byte[] getData() {
    return this.chunk_data;
  }

  public int getSize() {
    return this.chunk_size;
  }

  public boolean isFinalChunk() {
    return this.chunk_size < MAX_CHUNK_SIZE;
  }
}
