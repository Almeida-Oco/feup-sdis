package controller.client;

import network.Net_IO;
import controller.ApplicationInfo;
import controller.HandlerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Executor of the instructions received from the client
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public class Dispatcher implements HandlerInterface {
  Net_IO mc, mdb, mdr;
  private BackupHandler backup;
  private RestoreHandler restore;
  private DeleteHandler delete;
  private ReclaimHandler reclaim;
  private StateHandler state;

  /**
   * Creates a new {@link Dispatcher} with the given Multicast Channels
   * @param mc  MC {@link Net_IO}
   * @param mdb MDB {@link Net_IO}
   * @param mdr MDR {@link Net_IO}
   */
  public Dispatcher(Net_IO mc, Net_IO mdb, Net_IO mdr) {
    this.mc      = mc;
    this.mdr     = mdr;
    this.mdb     = mdb;
    this.backup  = new BackupHandler();
    this.restore = new RestoreHandler();
    this.delete  = new DeleteHandler();
    this.reclaim = new ReclaimHandler();
    this.state   = new StateHandler();
  }

  /**
   * Initializes the Backup protocol
   * @param  file_name       Path to file to be backed up
   * @param  rep_degree      Desired replication replication degree
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void backup(String file_name, int rep_degree) throws RemoteException {
    this.backup.start(file_name, rep_degree, mc, mdb);
  }

  /**
   * Initializes the Restore protocol
   * @param  file_name       Path to file to be restored
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void restore(String file_name) throws RemoteException {
    this.restore.start(file_name, mc, mdr);
  }

  /**
   * Initializes the Delete protocol
   * @param  file_name       Path to file to be deleted
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void delete(String file_name) throws RemoteException {
    this.delete.start(file_name, mc);
  }

  /**
   * Initializes the Reclaim protocol
   * @param  size            Number of bytes to be reclaimed
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void reclaim(String size) throws RemoteException {
    this.reclaim.start(Integer.parseInt(size), mc);
  }

  /**
   * Initializes the State protocol
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void state() throws RemoteException {
    this.state.run();
  }
}
