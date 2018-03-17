package controller.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class Handler implements HandlerInterface {
  private BackupHandler backup;
  private RestoreHandler restore;
  private DeleteHandler delete;
  private ReclaimHandler reclaim;
  private StateHandler state;

  public Handler() {
    this.backup  = new BackupHandler();
    this.restore = new RestoreHandler();
    this.delete  = new DeleteHandler();
    this.reclaim = new ReclaimHandler();
    this.state   = new StateHandler();
  }

  public void backup(String file_name, int rep_degree) throws RemoteException {
    this.backup.run();
  }

  public void restore(String file_name) throws RemoteException {
    this.restore.run();
  }

  public void delete(String file_name) throws RemoteException {
    this.delete.run();
  }

  public void reclaim(String file_name) throws RemoteException {
    this.reclaim.run();
  }

  public void state() throws RemoteException {
    this.state.run();
  }
}
