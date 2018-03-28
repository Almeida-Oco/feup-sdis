package controller;

import network.Net_IO;

/**
 * Holds all the information about the currently running program
 * @author Gonçalo Moreno
 * @author João Almeida
 * This class follows the singleton design pattern so there is only one instance throughout the entire program
 */
public class ApplicationInfo {
  /**
   * The ID of the running server
   */
  static int serv_id;

  /**
   * The version of the protocol
   */
  static byte version;

  /**
   * The multicast channels
   */
  static Net_IO mc, mdb, md;

  /**
   * The sole instance of {@link ApplicationInfo}
   */
  static ApplicationInfo instance;

  /**
   * Initializes a new {@link ApplicationInfo}
   */
  private ApplicationInfo() {
    serv_id  = -1;
    ap       = -1;
    version  = 0;
    instance = this;
  }

  /**
   * Sets the server ID
   * @param serv_id Server ID
   */
  public static void setServId(int serv_id) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.serv_id = serv_id;
  }

  /**
   * Sets the version of the protocol
   * @param version Version (major digit -> major version, minor digit -> minor version)
   */
  public static void setVersion(byte version) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.version = version;
  }

  /**
   * Sets the Multicast communication channels
   * @param mc  Multicast Control Channel
   * @param mdb Multicast Data Channel
   * @param mdr Multicast Data Recovery Channel
   */
  public static void setChannels(Net_IO mc, Net_IO mdb, Net_IO mdr) {
    if (instance == null) {
      instance = new ApplicationInfo();
    }
    instance.mc  = mc;
    instance.mdb = mdb;
    instance.mdr = mdr;
  }

  /**
   * Gets the server ID
   * @return {@link ApplicationInfo#serv_id}
   */
  public static int getServID() {
    return instance.serv_id;
  }

  /**
   * Gets the version of the protocol
   * @return {@link ApplicationInfo#version}
   */
  public static byte getVersion() {
    return instance.version;
  }

  /**
   * Gets the MC channel
   * @return {@link ApplicationInfo#mc}
   */
  public static Net_IO getMC() {
    return instance.mc;
  }

  /**
   * Gets the MDB channel
   * @return {@link ApplicationInfo#mdb}
   */
  public static Net_IO getMDB() {
    return instance.mdb;
  }

  /**
   * Gets the MDR channel
   * @return {@link ApplicationInfo#mdr}
   */
  public static Net_IO getMDR() {
    return instance.mdr;
  }
}
