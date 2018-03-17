package controller;

import network.Net_IO;

//  Singleton class
public class ApplicationInfo {
  static int serv_id;
  static String ap;
  static byte version;
  static Net_IO mc, mdb, mdr;
  static ApplicationInfo instance;

  private ApplicationInfo() {
    serv_id  = -1;
    ap       = null;
    version  = 0;
    instance = this;
  }

  public static void setServId(int serv_id) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.serv_id = serv_id;
  }

  public static void setAP(String ap) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.ap = ap;
  }

  public static void setVersion(byte version) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.version = version;
  }

  public static void setChannels(Net_IO mc, Net_IO mdb, Net_IO mdr) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.mc  = mc;
    instance.mdb = mdb;
    instance.mdr = mdr;
  }

  public static int getServID() {
    return instance.serv_id;
  }

  public static String getAP() {
    return instance.ap;
  }

  public static byte getVersion() {
    return instance.version;
  }

  public static Net_IO getMC() {
    return instance.mc;
  }

  public static Net_IO getMDB() {
    return instance.mdb;
  }

  public static Net_IO getMDR() {
    return instance.mdr;
  }
}
