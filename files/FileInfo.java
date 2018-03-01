package files;

//Maybe this wont be needed after all
class FileInfo {
  String file_name;
  byte version; /// Two numbered byte, unit value represents minor version, decimal value the major version

  FileInfo(String name, byte ver) {
    this.file_name = name;
    this.version   = ver;
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
}
