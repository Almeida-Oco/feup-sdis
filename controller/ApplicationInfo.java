package controller;

public class ApplicationInfo {
  int serv_id;
  String ap;
  byte version;

  ApplicationInfo(int id, String ap, byte ver) {
    this.serv_id = id;
    this.ap      = ap;
    this.version = ver;
  }

  int getServID() {
    return this.serv_id;
  }

  String getAP() {
    return this.ap;
  }

  byte getVersion() {
    return this.version;
  }
}
