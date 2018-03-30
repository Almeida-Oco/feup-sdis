package controller;

import controller.ChannelListener;
import network.Net_IO;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Holds all the information about the currently running program
 * @author Gonçalo Moreno
 * @author João Almeida
 * This class follows the singleton design pattern so there is only one instance throughout the entire program
 */
public class ApplicationInfo {
  private static final int cores     = Runtime.getRuntime().availableProcessors();
  private static final int MAX_TASKS = 255;

  /** The ID of the running server */
  static int serv_id;

  /** The version of the protocol */
  static byte version;

  /** The multicast channels listeners */
  static ChannelListener mc, mdb, mdr;

  /** The sole instance of {@link ApplicationInfo} */
  static ApplicationInfo instance;

  /**
   * Initializes a new {@link ApplicationInfo}
   */
  private ApplicationInfo() {
    serv_id  = -1;
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
    LinkedBlockingQueue<Runnable> queue      = new LinkedBlockingQueue<Runnable>(MAX_TASKS);
    ThreadPoolExecutor            task_queue = new ThreadPoolExecutor(cores, cores, 0, TimeUnit.SECONDS, queue);

    instance.mc  = new ChannelListener(mc, task_queue);
    instance.mdb = new ChannelListener(mdb, task_queue);
    instance.mdr = new ChannelListener(mdr, task_queue);
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
  public static ChannelListener getMC() {
    return instance.mc;
  }

  /**
   * Gets the MDB channel
   * @return {@link ApplicationInfo#mdb}
   */
  public static ChannelListener getMDB() {
    return instance.mdb;
  }

  /**
   * Gets the MDR channel
   * @return {@link ApplicationInfo#mdr}
   */
  public static ChannelListener getMDR() {
    return instance.mdr;
  }
}
