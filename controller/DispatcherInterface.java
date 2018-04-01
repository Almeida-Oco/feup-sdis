package controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface class
 * @author Gonçalo Moreno
 * @author João Almeida
 */
public interface DispatcherInterface extends Remote {
  /**
   * Initializes the backup protocol
   * @param  file_name       Path to file to be backed up
   * @param  rep_degree      Desired replication degree
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void backup(String file_name, int rep_degree) throws RemoteException;

  /**
   * Initializes the restore protocol
   * @param  file_name       Path to file to be restored
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void restore(String file_name) throws RemoteException;

  /**
   * Initializes the delete protocol
   * @param  file_name       Path to file to be deleted
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void delete(String file_name) throws RemoteException;

  /**
   * Initializes the reclaim protocol
   * @param  size            Bytes to reclaim
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void reclaim(String size) throws RemoteException;

  /**
   * Initializes the state protocol
   * @throws RemoteException Exception thrown when something goes wrong with RMI
   */
  public void state() throws RemoteException;
}
