package controller.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import controller.ApplicationInfo;
import network.Net_IO;

public class Handler implements HandlerInterface {
  Net_IO mc, mdb, mdr;
  private BackupHandler backup;
  private RestoreHandler restore;
  private DeleteHandler delete;
  private ReclaimHandler reclaim;
  private StateHandler state;

  public Handler(Net_IO mc, Net_IO mdb, Net_IO mdr) {
    this.mc      = mc;
    this.mdr     = mdr;
    this.mdb     = mdb;
    this.backup  = new BackupHandler();
    this.restore = new RestoreHandler();
    this.delete  = new DeleteHandler();
    this.reclaim = new ReclaimHandler();
    this.state   = new StateHandler();
  }

  public void backup(String file_name, int rep_degree) throws RemoteException {
    this.backup.start(file_name, rep_degree, mc, mdb);
  }

  public void restore(String file_name) throws RemoteException {
    this.restore.start(file_name, mc, mdr);
  }

  public void delete(String file_name) throws RemoteException {
    this.delete.start(file_name, mc);
  }

  public void reclaim(String size) throws RemoteException {
    this.reclaim.start(Integer.parseInt(size), mc, mdb);
  }

  public void state() throws RemoteException {
    this.state.run();
  }
}
