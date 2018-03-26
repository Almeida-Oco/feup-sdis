package controller;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HandlerInterface extends Remote {
  public void backup(String file_name, int rep_degree) throws RemoteException;
  public void restore(String file_name) throws RemoteException;
  public void delete(String file_name) throws RemoteException;
  public void reclaim(String file_name) throws RemoteException;
  public void state() throws RemoteException;
}
