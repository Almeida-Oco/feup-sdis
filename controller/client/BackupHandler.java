package controller.client;

import java.rmi.Remote;

class BackupHandler implements Remote {
  void backup(String f_name) {
    System.out.println("GOT BACKUP MSG! '" + f_name + "'");
  }
}
