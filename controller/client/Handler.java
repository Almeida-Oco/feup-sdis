package controller.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public class Handler implements Remote {
  private BackupHandler backup;
  private RestoreHandler restore;
  private DeleteHandler delete;
  private ReclaimHandler reclaim;
  private StateHandler state;

  public void backup(String file_name) {
    this.backup.backup(file_name);
  }

  public void restore(String file_name) {
    this.restore.restore(file_name);
  }

  public void delete(String file_name) {
    this.delete.delete(file_name);
  }

  public void reclaim(String file_name) {
    this.reclaim.reclaim(file_name);
  }

  public void state(String file_name) {
    this.state.state(file_name);
  }
}
