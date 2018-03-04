package files;

//Maybe this wont be needed after all
public class FileInfo {
  String file_name;
  Vector<FileChunk> chunks;

  FileInfo(String name, int chunk_number) {
    this.file_name = name;
    this.chunks    = Vector<FileChunk>(chunk_number);
  }

  void addChunk(FileChunk chunk) {
    this.chunk.push(chunk);
  }

  public boolean equals(Object obj) {
    if ((obj == null) || (this.getClass() != obj.getClass())) {
      return false;
    }
    else {
      return ((FileInfo)obj).file_name.equals(this.file_name) && ((FileInfo)obj).version == this.version;
    }
  }

  public String getName() {
    return this.file_name;
  }

  public Vector<FileChunk> getChunks() {
    return this.chunks;
  }
}
